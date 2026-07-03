package com.gymsync.data.model.request

data class ResetPasswordRequest(
    val userId: String,
    val newPassword: String
)

data class SetPetRequest(
    val name: String,
    val type: String,
    val color: String? = null
)

data class AiChatRequest(
    val message: String,
    val petType: Int,
    val petName: String,
    val userName: String? = null,
    val context: String? = null
)
