package com.gymsync.data.model.request

data class LoginRequest(
    val username: String,
    val password: String
)

data class ActivateRequest(
    val inviteToken: String,
    val username: String,
    val displayName: String,
    val password: String,
    val email: String
)

data class ActivateWithPetRequest(
    val inviteToken: String,
    val username: String,
    val displayName: String,
    val password: String,
    val email: String,
    val petName: String,
    val petType: String,
    val petColor: String? = null
)

data class RefreshRequest(
    val refreshToken: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
