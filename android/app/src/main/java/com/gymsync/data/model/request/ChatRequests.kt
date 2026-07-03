package com.gymsync.data.model.request

data class SendMessageRequest(
    val textContent: String? = null,
    val imageBase64: String? = null,
    val imageContentType: String? = null
)

data class MotivationRequest(
    val type: String,
    val customMessage: String? = null
)
