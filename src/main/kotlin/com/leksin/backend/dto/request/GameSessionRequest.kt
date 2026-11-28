package com.leksin.backend.dto.request

import com.leksin.backend.entity.GameType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

@Schema(description = "Result and metadata for one child learning game session.")
data class GameSessionRequest(
    @field:NotNull
    @field:Schema(description = "Game mode played.", example = "WORD_SCRAMBLE")
    val gameType: GameType,

    @field:Min(1)
    @field:Max(26)
    @field:Schema(description = "Level number, from 1 to 26.", example = "3", minimum = "1", maximum = "26")
    val level: Int,

    @field:Min(0)
    @field:Schema(description = "Duration of the session in seconds.", example = "95", minimum = "0")
    val durationSeconds: Int,

    @field:Min(1)
    @field:Schema(description = "Number of attempts made during the session.", example = "2", minimum = "1")
    val attempts: Int,

    @field:Min(0)
    @field:Max(3)
    @field:Schema(description = "Stars earned, from 0 to 3.", example = "2", minimum = "0", maximum = "3")
    val starsEarned: Int,

    @field:Schema(description = "Whether the game session was completed.", example = "true")
    val completed: Boolean,
)
