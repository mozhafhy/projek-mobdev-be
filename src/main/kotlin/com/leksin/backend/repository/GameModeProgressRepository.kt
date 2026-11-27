package com.leksin.backend.repository

import com.leksin.backend.entity.GameModeProgress
import com.leksin.backend.entity.GameType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GameModeProgressRepository : JpaRepository<GameModeProgress, UUID> {
    fun findByChildIdAndGameType(
        childId: UUID,
        gameType: GameType,
    ): GameModeProgress?

    fun deleteAllByChildId(childId: UUID)
}
