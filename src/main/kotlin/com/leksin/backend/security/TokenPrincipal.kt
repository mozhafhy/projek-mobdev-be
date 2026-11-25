package com.leksin.backend.security

import com.leksin.backend.entity.Role
import java.time.Instant
import java.util.UUID

data class TokenPrincipal(
    val userId: UUID,
    val role: Role,
    val tokenId: UUID,
    val expiresAt: Instant,
)
