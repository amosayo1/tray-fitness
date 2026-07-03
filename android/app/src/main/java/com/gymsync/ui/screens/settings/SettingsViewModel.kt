package com.gymsync.ui.screens.settings

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

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val showChangePassword: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val repository: GymSyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(isDarkMode = tokenManager.darkMode))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleDarkMode() {
        val newValue = !_uiState.value.isDarkMode
        tokenManager.darkMode = newValue
        _uiState.value = _uiState.value.copy(isDarkMode = newValue)
    }

    fun showChangePassword() {
        _uiState.value = _uiState.value.copy(
            showChangePassword = true,
            currentPassword = "",
            newPassword = "",
            confirmPassword = ""
        )
    }

    fun hideChangePassword() {
        _uiState.value = _uiState.value.copy(showChangePassword = false, message = null)
    }

    fun updateCurrentPassword(v: String) {
        _uiState.value = _uiState.value.copy(currentPassword = v, message = null)
    }

    fun updateNewPassword(v: String) {
        _uiState.value = _uiState.value.copy(newPassword = v, message = null)
    }

    fun updateConfirmPassword(v: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = v, message = null)
    }

    fun changePassword() {
        val state = _uiState.value
        if (state.newPassword != state.confirmPassword) {
            _uiState.value = state.copy(message = "Passwords do not match")
            return
        }
        if (state.newPassword.length < 6) {
            _uiState.value = state.copy(message = "Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            repository.changePassword(state.currentPassword, state.newPassword).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showChangePassword = false,
                        message = "Password changed successfully"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = e.message ?: "Failed to change password"
                    )
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
