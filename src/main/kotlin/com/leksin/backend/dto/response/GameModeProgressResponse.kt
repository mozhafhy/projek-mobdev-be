package com.leksin.backend.dto.response

import com.leksin.backend.entity.GameType
import java.util.UUID

data class GameModeProgressResponse(
    val childId: UUID,
    val gameType: GameType,
    val highestLevel: Int,
    val totalStars: Int,
)
