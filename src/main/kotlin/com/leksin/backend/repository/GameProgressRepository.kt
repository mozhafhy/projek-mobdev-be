package com.leksin.backend.repository

import com.leksin.backend.entity.GameProgress
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GameProgressRepository : JpaRepository<GameProgress, UUID> {
    fun findByChildId(childId: UUID): GameProgress?

    fun deleteAllByChildId(childId: UUID)
}
