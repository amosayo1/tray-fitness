package com.gymsync.ui.screens.home

import android.app.Application
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymsync.data.local.TokenManager
import com.gymsync.data.model.response.DailyStepLogDto
import com.gymsync.data.model.response.HomeDataResponse
import com.gymsync.data.model.response.PetDto
import com.gymsync.data.repository.GymSyncRepository
import com.gymsync.service.SignalRService
import com.gymsync.service.StepCounterService
import com.gymsync.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val homeData: HomeDataResponse? = null,
    val pet: PetDto? = null,
    val isAdmin: Boolean = false,
    val steps: DailyStepLogDto? = null,
    val liveStepCount: Int = 0,
    val userDisplayName: String = "",
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GymSyncRepository,
    private val tokenManager: TokenManager,
    private val signalR: SignalRService,
    private val application: Application
) : ViewModel() {

    var uiState by mutableStateOf(HomeUiState())
        private set

    private var lastOnlineStatus: String? = null
    private var syncJob: Job? = null
    private var lastSyncedSteps: Int = 0

    init {
        uiState = uiState.copy(
            isAdmin = isAdminFromToken(),
            userDisplayName = extractDisplayName()
        )
        loadData()
        observePartnerStatus()
        startStepTracking()
    }

    private fun isAdminFromToken(): Boolean {
        val token = tokenManager.accessToken ?: return false
        try {
            val parts = token.split(".")
            if (parts.size < 2) return false
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)
            return json.optString("isAdmin") == "true" || json.optBoolean("isAdmin", false)
        } catch (_: Exception) {
            return false
        }
    }

    private fun extractDisplayName(): String {
        val token = tokenManager.accessToken ?: return ""
        try {
            val parts = token.split(".")
            if (parts.size < 2) return ""
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)
            return json.optString("displayName") ?: json.optString("sub") ?: ""
        } catch (_: Exception) {
            return ""
        }
    }

    private fun observePartnerStatus() {
        viewModelScope.launch {
            signalR.partnerStatus.collect { event ->
                val partnerName = uiState.homeData?.partnerProfile?.displayName ?: "Your partner"
                if (event.status == "Online" && lastOnlineStatus != "Online") {
                    NotificationHelper.showPartnerOnlineNotification(
                        application, partnerName
                    )
                }
                lastOnlineStatus = event.status
            }
        }
        viewModelScope.launch {
            signalR.newMessage.collect { event ->
                val partnerName = uiState.homeData?.partnerProfile?.displayName ?: "Your partner"
                NotificationHelper.showPartnerWorkoutNotification(
                    application, partnerName
                )
            }
        }
    }

    private fun startStepTracking() {
        StepCounterService.start(application)
        viewModelScope.launch {
            StepCounterService.stepCount.collect { steps ->
                uiState = uiState.copy(liveStepCount = steps)
            }
        }
        syncJob = viewModelScope.launch {
            while (true) {
                delay(30000)
                val currentSteps = StepCounterService.currentSteps
                if (currentSteps != lastSyncedSteps) {
                    lastSyncedSteps = currentSteps
                    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    repository.logSteps(today, currentSteps)
                }
            }
        }
    }

    fun sendPetChat(
        message: String,
        petType: Int,
        petName: String,
        userName: String?,
        context: String?,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            repository.aiChat(message, petType, petName, userName, context).fold(
                onSuccess = { response ->
                    onSuccess(response.message)
                },
                onFailure = {
                    onError()
                }
            )
        }
    }

    fun loadData() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            repository.getHomeData().fold(
                onSuccess = { homeData ->
                    uiState = uiState.copy(
                        homeData = homeData,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    uiState = uiState.copy(isLoading = false, error = e.message)
                }
            )
        }
        viewModelScope.launch {
            repository.getMyPet().fold(
                onSuccess = { pet ->
                    uiState = uiState.copy(pet = pet)
                },
                onFailure = { }
            )
        }
        viewModelScope.launch {
            repository.getStepHistory(days = 1).fold(
                onSuccess = { logs ->
                    val todayLog = logs.firstOrNull { it.date.startsWith(today) }
                    uiState = uiState.copy(steps = todayLog)
                },
                onFailure = { }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        syncJob?.cancel()
        StepCounterService.stop(application)
    }
}
