package com.gymsync.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymsync.data.local.TokenManager
import com.gymsync.data.repository.GymSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: GymSyncRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        if (tokenManager.isLoggedIn && tokenManager.accessToken != null) {
            _uiState.value = AuthUiState(isAuthenticated = true)
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = repository.login(username, password)
            result.fold(
                onSuccess = { tokenResponse ->
                    tokenManager.accessToken = tokenResponse.accessToken
                    tokenManager.refreshToken = tokenResponse.refreshToken
                    tokenManager.isLoggedIn = true
                    _uiState.value = AuthUiState(isAuthenticated = true)
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState(error = e.message ?: "Login failed")
                }
            )
        }
    }

    fun activate(
        token: String,
        username: String,
        displayName: String,
        password: String,
        email: String,
        petName: String? = null,
        petType: String? = null,
        petColor: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = if (petName != null && petType != null) {
                repository.activateWithPet(token, username, displayName, password, email, petName, petType, petColor)
            } else {
                repository.activate(token, username, displayName, password, email)
            }
            result.fold(
                onSuccess = { tokenResponse ->
                    tokenManager.accessToken = tokenResponse.accessToken
                    tokenManager.refreshToken = tokenResponse.refreshToken
                    tokenManager.isLoggedIn = true
                    if (petName != null && petType != null) {
                        viewModelScope.launch {
                            repository.setPet(petName, petType, petColor)
                        }
                    }
                    _uiState.value = AuthUiState(isAuthenticated = true)
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState(error = e.message ?: "Activation failed")
                }
            )
        }
    }

    fun logout() {
        tokenManager.clear()
        _uiState.value = AuthUiState()
    }
}
