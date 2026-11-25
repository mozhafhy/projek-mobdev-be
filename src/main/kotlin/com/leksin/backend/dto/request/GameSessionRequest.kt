package com.leksin.backend.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class GameSessionRequest(

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
