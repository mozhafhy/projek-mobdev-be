package com.leksin.backend.dto

import java.util.UUID

data class ParentResponse(
    val id: UUID,
    val username: String,
    val email: String,
)
