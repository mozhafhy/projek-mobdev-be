package com.leksin.backend.dto.request

import com.leksin.backend.entity.GameType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class GameSessionRequest(

    @field:NotNull
    val gameType: GameType,

    @field:Min(1)
    @field:Max(26)
    val level: Int,

    @field:Min(0)
    val durationSeconds: Int,

    @field:Min(1)
    val attempts: Int,

    @field:Min(0)
    @field:Max(3)
    val starsEarned: Int,

    val completed: Boolean,
)
