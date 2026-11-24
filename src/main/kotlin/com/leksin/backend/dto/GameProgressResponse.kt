package com.leksin.backend.dto

import java.time.LocalDate
import java.util.UUID

data class GameProgressResponse(
    val childId: UUID,
    val highestLevel: Int,
    val totalStars: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActivityDate: LocalDate?,
)
