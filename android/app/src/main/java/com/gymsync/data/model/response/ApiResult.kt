package com.gymsync.data.model.response

data class ApiResult<T>(
    val succeeded: Boolean,
    val data: T?,
    val errors: List<String>? = null
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: String
)
