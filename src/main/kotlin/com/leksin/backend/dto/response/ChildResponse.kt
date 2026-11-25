package com.leksin.backend.dto.response

import java.util.UUID

data class ChildResponse(
    val id: UUID,
    val nickname: String,
)
