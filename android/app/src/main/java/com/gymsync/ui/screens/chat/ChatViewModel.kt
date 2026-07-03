package com.gymsync.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymsync.data.local.TokenManager
import com.gymsync.data.model.response.MessageResponse
import com.gymsync.data.repository.GymSyncRepository
import com.gymsync.service.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<MessageResponse> = emptyList(),
    val partnerName: String = "Partner",
    val partnerStatus: String = "Online",
    val isTyping: Boolean = false,
    val showMotivationPanel: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: GymSyncRepository,
    private val tokenManager: TokenManager,
    private val signalR: SignalRService
) : ViewModel() {

    val myUserId: String get() = tokenManager.userId ?: ""

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
        observeSignalR()
    }

    private fun observeSignalR() {
        viewModelScope.launch {
            signalR.newMessage.collect { event ->
                loadMessages()
                signalR.markDelivered(event.messageId)
            }
        }

        viewModelScope.launch {
            signalR.messageRead.collect { event ->
                val updated = _uiState.value.messages.map { msg ->
                    if (msg.id == event.messageId) msg.copy(isRead = true) else msg
                }
                _uiState.value = _uiState.value.copy(messages = updated)
            }
        }

        viewModelScope.launch {
            signalR.partnerTyping.collect { event ->
                _uiState.value = _uiState.value.copy(isTyping = event.isTyping)
            }
        }

        viewModelScope.launch {
            signalR.partnerStatus.collect { event ->
                _uiState.value = _uiState.value.copy(partnerStatus = event.status)
            }
        }

        viewModelScope.launch {
            signalR.motivation.collect { event ->
                loadMessages()
            }
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
            repository.getMessages().fold(
                onSuccess = { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages.reversed(),
                        isLoading = false
                    )
                    messages.forEach { msg ->
                        if (msg.senderId != myUserId && !msg.isRead) {
                            signalR.markRead(msg.id)
                        }
                    }
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            repository.sendMessage(text = text).fold(
                onSuccess = { msg ->
                    loadMessages()
                },
                onFailure = { /* handle error */ }
            )
        }
    }

    fun sendMotivation(type: String, message: String?) {
        viewModelScope.launch {
            repository.sendMotivation(type, message)
            toggleMotivationPanel()
        }
    }

    fun setTyping(isTyping: Boolean) {
        signalR.setTyping(isTyping)
    }

    fun toggleMotivationPanel() {
        _uiState.value = _uiState.value.copy(
            showMotivationPanel = !_uiState.value.showMotivationPanel
        )
    }
}
