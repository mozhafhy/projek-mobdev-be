package com.leksin.backend.dto.response

import java.util.UUID

data class ChildLoginResponse(
    val childId: UUID,
    val nickname: String,
)
