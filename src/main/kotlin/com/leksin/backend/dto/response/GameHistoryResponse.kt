package com.leksin.backend.dto.response

import java.time.Instant
import java.util.UUID

data class GameHistoryResponse(
    val id: UUID,
    val level: Int,
    val durationSeconds: Int,
    val attempts: Int,
    val starsEarned: Int,
    val completed: Boolean,
    val playedAt: Instant,
)
