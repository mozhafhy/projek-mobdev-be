package com.leksin.backend.service

import com.leksin.backend.dto.request.GameSessionRequest
import com.leksin.backend.dto.response.GameHistoryResponse
import com.leksin.backend.dto.response.GameModeProgressResponse
import com.leksin.backend.dto.response.GameProgressResponse
import com.leksin.backend.entity.GameHistory
import com.leksin.backend.entity.GameModeProgress
import com.leksin.backend.entity.GameProgress
import com.leksin.backend.entity.GameType
import com.leksin.backend.entity.toResponse
import com.leksin.backend.exception.ChildNotFoundException
import com.leksin.backend.repository.ChildRepository
import com.leksin.backend.repository.GameHistoryRepository
import com.leksin.backend.repository.GameModeProgressRepository
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
    private val gameModeProgressRepository: GameModeProgressRepository,
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

        val child = childRepository.lockById(childId) ?: throw ChildNotFoundException()

        val previousBestStars = gameHistoryRepository.findBestStars(
            childId = childId,
            gameType = request.gameType,
            level = request.level,
        )

        val history = gameHistoryRepository.save(
            GameHistory(
                child = child,
                gameType = request.gameType,
                level = request.level,
                durationSeconds = request.durationSeconds,
                attempts = request.attempts,
                starsEarned = request.starsEarned,
                completed = request.completed,
            )
        )

        if (request.completed) {
            val earnedStars = newStarsEarned(request.starsEarned, previousBestStars)
            updateModeProgress(childId, child, request, earnedStars)
            updateOverallProgress(child, request, earnedStars)
        }

        return history.toResponse()
    }

    @Transactional(readOnly = true)
    fun getHistory(
        childId: UUID,
        gameType: GameType?,
    ): List<GameHistoryResponse> {
        requireChildExists(childId)

        val history = if (gameType == null) {
            gameHistoryRepository.findAllByChildIdOrderByPlayedAtDesc(childId)
        } else {
            gameHistoryRepository.findAllByChildIdAndGameTypeOrderByPlayedAtDesc(
                childId,
                gameType,
            )
        }

        return history.map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getProgress(childId: UUID): GameProgressResponse {
        requireChildExists(childId)

        return gameProgressRepository.findByChildId(childId)?.toResponse()
            ?: emptyProgress(childId)
    }

    @Transactional(readOnly = true)
    fun getModeProgress(
        childId: UUID,
        gameType: GameType,
    ): GameModeProgressResponse {
        requireChildExists(childId)

        return gameModeProgressRepository.findByChildIdAndGameType(childId, gameType)
            ?.toResponse()
            ?: GameModeProgressResponse(
                childId = childId,
                gameType = gameType,
                highestLevel = 0,
                totalStars = 0,
            )
    }

    private fun updateModeProgress(
        childId: UUID,
        child: com.leksin.backend.entity.Children,
        request: GameSessionRequest,
        earnedStars: Int,
    ) {
        val progress = gameModeProgressRepository.findByChildIdAndGameType(
            childId,
            request.gameType,
        ) ?: GameModeProgress(child = child, gameType = request.gameType)

        progress.highestLevel = maxOf(progress.highestLevel, request.level)
        progress.totalStars += earnedStars
        gameModeProgressRepository.save(progress)
    }

    private fun updateOverallProgress(
        child: com.leksin.backend.entity.Children,
        request: GameSessionRequest,
        earnedStars: Int,
    ) {
        val progress = gameProgressRepository.findByChildId(child.id!!) ?: GameProgress(child = child)

        progress.highestLevel = maxOf(progress.highestLevel, request.level)
        progress.totalStars += earnedStars
        updateStreak(progress)
        gameProgressRepository.save(progress)
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
