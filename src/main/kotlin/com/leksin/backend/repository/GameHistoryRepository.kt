package com.leksin.backend.repository

import com.leksin.backend.entity.GameHistory
import com.leksin.backend.entity.GameType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface GameHistoryRepository : JpaRepository<GameHistory, UUID> {
    fun findAllByChildIdOrderByPlayedAtDesc(
        childId: UUID,
    ): List<GameHistory>

    fun findAllByChildIdAndGameTypeOrderByPlayedAtDesc(
        childId: UUID,
        gameType: GameType,
    ): List<GameHistory>

    @Query(
        """
        select coalesce(max(h.starsEarned), 0)
        from GameHistory h
        where h.child.id = :childId
          and h.gameType = :gameType
          and h.level = :level
          and h.completed = true
        """
    )
    fun findBestStars(
        @Param("childId") childId: UUID,
        @Param("gameType") gameType: GameType,
        @Param("level") level: Int,
    ): Int

    fun deleteAllByChildId(childId: UUID)
}
