package com.leksin.backend.dto.response

import com.leksin.backend.entity.GameType
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Learning progress for one game mode.")
data class GameModeProgressResponse(
    @field:Schema(description = "Child profile identifier.")
    val childId: UUID,

    @field:Schema(description = "Game mode.", example = "HANDWRITING")
    val gameType: GameType,

    @field:Schema(description = "Highest level completed in this mode.", example = "5")
    val highestLevel: Int,

    @field:Schema(description = "Total stars earned in this mode.", example = "12")
    val totalStars: Int,
)
