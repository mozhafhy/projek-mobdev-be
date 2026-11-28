package com.leksin.backend.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

@Schema(description = "Aggregated learning progress across all game modes.")
data class GameProgressResponse(
    @field:Schema(description = "Child profile identifier.")
    val childId: UUID,

    @field:Schema(description = "Highest level completed.", example = "8")
    val highestLevel: Int,

    @field:Schema(description = "Total stars earned across game modes.", example = "21")
    val totalStars: Int,

    @field:Schema(description = "Current consecutive-day activity streak.", example = "3")
    val currentStreak: Int,

    @field:Schema(description = "Longest consecutive-day activity streak.", example = "7")
    val longestStreak: Int,

    @field:Schema(description = "Date of the most recent activity, or null when there is no activity.", example = "2026-11-28", nullable = true)
    val lastActivityDate: LocalDate?,
)
