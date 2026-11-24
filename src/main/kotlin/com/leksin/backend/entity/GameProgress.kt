package com.leksin.backend.entity

import com.leksin.backend.dto.GameProgressResponse
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "game_progress")
class GameProgress(

    @Id
    @Column(name = "child_id")
    var id: UUID? = null,

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_id")
    var child: Children,

    var highestLevel: Int = 0,
    var totalStars: Int = 0,
    var currentStreak: Int = 0,
    var longestStreak: Int = 0,
    var lastActivityDate: LocalDate? = null,
)

fun GameProgress.toResponse() = GameProgressResponse(
    childId = child.id!!,
    highestLevel = highestLevel,
    totalStars = totalStars,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastActivityDate = lastActivityDate,
)
