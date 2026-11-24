package com.leksin.backend.entity

import com.leksin.backend.dto.GameHistoryResponse
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "game_history")
class GameHistory(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(
        name = "child_id",
        nullable = false
    ) var child: Children,

    @Column(nullable = false) var level: Int,

    @Column(nullable = false) var durationSeconds: Int,

    @Column(nullable = false) var attempts: Int,

    @Column(nullable = false) var starsEarned: Int,

    @Column(nullable = false) var completed: Boolean,

    @Column(nullable = false) var playedAt: Instant = Instant.now(),
)

fun GameHistory.toResponse() = GameHistoryResponse(
    id = this.id!!,
    level = this.level,
    durationSeconds = this.durationSeconds,
    attempts = this.attempts,
    starsEarned = this.starsEarned,
    completed = this.completed,
    playedAt = this.playedAt,
)
