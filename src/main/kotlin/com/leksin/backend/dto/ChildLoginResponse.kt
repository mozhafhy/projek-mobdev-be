package com.leksin.backend.dto

import java.util.UUID

data class ChildLoginResponse(
    val childId: UUID,
    val nickname: String,
)
