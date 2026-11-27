package com.leksin.backend.entity

import com.leksin.backend.dto.response.GameModeProgressResponse
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "game_mode_progress",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_game_mode_progress_child_type",
            columnNames = ["child_id", "game_type"],
        ),
    ],
)
class GameModeProgress(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_id", nullable = false)
    var child: Children,

    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false, length = 32)
    var gameType: GameType,

    @Column(nullable = false)
    var highestLevel: Int = 0,

    @Column(nullable = false)
    var totalStars: Int = 0,
)

fun GameModeProgress.toResponse() = GameModeProgressResponse(
    childId = child.id!!,
    gameType = gameType,
    highestLevel = highestLevel,
    totalStars = totalStars,
)
