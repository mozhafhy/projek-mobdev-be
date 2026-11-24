package com.leksin.backend.service

import com.leksin.backend.dto.GameHistoryResponse
import com.leksin.backend.dto.GameProgressResponse
import com.leksin.backend.dto.GameSessionRequest
import com.leksin.backend.entity.GameHistory
import com.leksin.backend.entity.GameProgress
import com.leksin.backend.entity.toResponse
import com.leksin.backend.exception.ChildNotFoundException
import com.leksin.backend.repository.ChildRepository
import com.leksin.backend.repository.GameHistoryRepository
import com.leksin.backend.repository.GameProgressRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@Service
class GameService(
    private val childRepository: ChildRepository,
    private val gameHistoryRepository: GameHistoryRepository,
    private val gameProgressRepository: GameProgressRepository,
) {
    private val applicationZone = ZoneId.of("Asia/Jakarta")

    @Transactional
    fun recordSession(
        childId: UUID,
        request: GameSessionRequest,
    ): GameHistoryResponse {
        require(request.completed || request.starsEarned == 0) {
            "Incomplete sessions cannot earn stars"
        }

        val child = childRepository.findById(childId).orElseThrow { ChildNotFoundException() }

        val previousBestStars = gameHistoryRepository.findBestStars(childId, request.level)

        val history = gameHistoryRepository.save(
            GameHistory(
                child = child,
                level = request.level,
                durationSeconds = request.durationSeconds,
                attempts = request.attempts,
                starsEarned = request.starsEarned,
                completed = request.completed,
            )
        )

        val progress = gameProgressRepository.findByChildId(childId) ?: GameProgress(child = child)

        if (request.completed) {
            updateProgress(progress, request, previousBestStars)
        }

        gameProgressRepository.save(progress)

        return history.toResponse()
    }

    @Transactional(readOnly = true)
    fun getHistory(childId: UUID): List<GameHistoryResponse> {
        requireChildExists(childId)

        return gameHistoryRepository
            .findAllByChildIdOrderByPlayedAtDesc(childId)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getProgress(childId: UUID): GameProgressResponse {
        requireChildExists(childId)

        return gameProgressRepository.findByChildId(childId)?.toResponse()
            ?: emptyProgress(childId)
    }

    private fun requireChildExists(childId: UUID) {
        if (!childRepository.existsById(childId)) {
            throw ChildNotFoundException()
        }
    }

    private fun emptyProgress(childId: UUID) = GameProgressResponse(
        childId = childId,
        highestLevel = 0,
        totalStars = 0,
        currentStreak = 0,
        longestStreak = 0,
        lastActivityDate = null,
    )

    private fun updateProgress(
        progress: GameProgress,
        request: GameSessionRequest,
        previousBestStars: Int,
    ) {
        progress.highestLevel = maxOf(progress.highestLevel, request.level)
        progress.totalStars += newStarsEarned(request.starsEarned, previousBestStars)
        updateStreak(progress)
    }

    private fun newStarsEarned(
        starsEarned: Int,
        previousBestStars: Int,
    ): Int = maxOf(0, starsEarned - previousBestStars)

    private fun updateStreak(progress: GameProgress) {
        val today = LocalDate.now(applicationZone)

        when (progress.lastActivityDate) {
            today -> Unit
            today.minusDays(1) -> progress.currentStreak++
            else -> progress.currentStreak = 1
        }

        progress.lastActivityDate = today
        progress.longestStreak = maxOf(progress.longestStreak, progress.currentStreak)
    }
}
