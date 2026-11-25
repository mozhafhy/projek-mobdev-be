package com.leksin.backend.security

import com.leksin.backend.entity.Role
import java.util.UUID

data class TokenPrincipal(val userId: UUID, val role: Role)
