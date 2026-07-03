package com.gymsync.data.model.response

data class MessageResponse(
    val id: String,
    val senderId: String,
    val senderName: String,
    val textContent: String?,
    val imageUrl: String?,
    val voiceNoteUrl: String?,
    val voiceNoteDurationSeconds: Int?,
    val isRead: Boolean,
    val isDelivered: Boolean,
    val sentAt: String
)
