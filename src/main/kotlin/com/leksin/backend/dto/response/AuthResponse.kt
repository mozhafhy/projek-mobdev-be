package com.leksin.backend.dto.response

import java.util.UUID

data class AuthResponse(
    val userId: UUID,
    val username: String,
    val role: String,
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
)
