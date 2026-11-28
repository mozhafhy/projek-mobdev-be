package com.leksin.backend.dto.response

import com.leksin.backend.entity.GameType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "Recorded result of one learning game session.")
data class GameHistoryResponse(
    @field:Schema(description = "Game session identifier.")
    val id: UUID,

    @field:Schema(description = "Game mode.", example = "WORD_SCRAMBLE")
    val gameType: GameType,

    @field:Schema(description = "Level played.", example = "3")
    val level: Int,

    @field:Schema(description = "Session duration in seconds.", example = "95")
    val durationSeconds: Int,

    @field:Schema(description = "Number of attempts.", example = "2")
    val attempts: Int,

    @field:Schema(description = "Stars earned, from 0 to 3.", example = "2")
    val starsEarned: Int,

    @field:Schema(description = "Whether the session was completed.", example = "true")
    val completed: Boolean,

    @field:Schema(description = "Time when the session was recorded.", example = "2026-11-28T09:30:00Z")
    val playedAt: Instant,
)
