package com.leksin.backend.service

import com.leksin.backend.dto.request.GameSessionRequest
import com.leksin.backend.entity.*
import com.leksin.backend.exception.ChildNotFoundException
import com.leksin.backend.repository.ChildRepository
import com.leksin.backend.repository.GameHistoryRepository
import com.leksin.backend.repository.GameModeProgressRepository
import com.leksin.backend.repository.GameProgressRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameServiceTest {
    private val childRepository = Mockito.mock(ChildRepository::class.java)
    private val historyRepository = Mockito.mock(GameHistoryRepository::class.java)
    private val progressRepository = Mockito.mock(GameProgressRepository::class.java)
    private val modeProgressRepository = Mockito.mock(GameModeProgressRepository::class.java)
    private val parent = Parent(
        id = UUID.randomUUID(),
        username = "parent-test",
        password = "encoded-password",
        email = "parent@leksin.test",
    )
    private val child = Children(
        id = UUID.randomUUID(),
        nickname = "child-test",
        parent = parent,
    )
    private lateinit var service: GameService

    @BeforeEach
    fun setUp() {
        service = GameService(
            childRepository,
            historyRepository,
            progressRepository,
            modeProgressRepository,
        )
        Mockito.`when`(childRepository.lockById(child.id!!))
            .thenReturn(child)
        Mockito.`when`(historyRepository.save(Mockito.any(GameHistory::class.java)))
            .thenAnswer { simulatePersist(it.getArgument(0)) }
        Mockito.`when`(modeProgressRepository.save(Mockito.any(GameModeProgress::class.java)))
            .thenAnswer { it.getArgument(0) }
        Mockito.`when`(progressRepository.save(Mockito.any(GameProgress::class.java)))
            .thenAnswer { it.getArgument(0) }
        Mockito.`when`(historyRepository.findBestStars(child.id!!, GameType.WORD_SCRAMBLE, 1))
            .thenReturn(0)
    }

    @Test
    fun completedSessionUpdatesModeAndGlobalProgress() {
        Mockito.`when`(historyRepository.findBestStars(child.id!!, GameType.WORD_SCRAMBLE, 1))
            .thenReturn(0)

        val response = service.recordSession(child.id!!, request())
        val today = LocalDate.now(ZoneId.of("Asia/Jakarta"))

        assertEquals(GameType.WORD_SCRAMBLE, response.gameType)
        assertEquals(2, response.starsEarned)
        Mockito.verify(modeProgressRepository).save(Mockito.argThat {
            it.gameType == GameType.WORD_SCRAMBLE &&
                    it.highestLevel == 1 &&
                    it.totalStars == 2
        })
        Mockito.verify(progressRepository).save(Mockito.argThat {
            it.highestLevel == 1 &&
                    it.totalStars == 2 &&
                    it.currentStreak == 1 &&
                    it.lastActivityDate == today
        })
    }

    @Test
    fun replayingLevelOnlyAddsStarsAbovePreviousBest() {
        assertReplayTotalStars(previousBest = 1, starsEarned = 2, expectedTotal = 2)
    }

    @Test
    fun replayingLevelWithSameStarsAddsNothing() {
        assertReplayTotalStars(previousBest = 2, starsEarned = 2, expectedTotal = 2)
    }

    @Test
    fun gameModesUseSeparateBestScoresAndProgress() {
        Mockito.`when`(historyRepository.findBestStars(child.id!!, GameType.HANDWRITING, 1))
            .thenReturn(0)

        service.recordSession(child.id!!, request(GameType.HANDWRITING, 3))

        Mockito.verify(historyRepository).findBestStars(child.id!!, GameType.HANDWRITING, 1)
        Mockito.verify(modeProgressRepository).save(Mockito.argThat {
            it.gameType == GameType.HANDWRITING && it.totalStars == 3
        })
        Mockito.verify(progressRepository).save(Mockito.argThat { it.totalStars == 3 })
    }

    @Test
    fun incompleteSessionIsRecordedWithoutProgressChanges() {
        val response = service.recordSession(
            child.id!!,
            request(starsEarned = 0, completed = false),
        )

        assertFalse(response.completed)
        Mockito.verify(historyRepository).save(Mockito.any(GameHistory::class.java))
        Mockito.verifyNoInteractions(modeProgressRepository)
        Mockito.verifyNoInteractions(progressRepository)
    }

    @Test
    fun incompleteSessionCannotEarnStars() {
        assertThrows<IllegalArgumentException> {
            service.recordSession(child.id!!, request(starsEarned = 1, completed = false))
        }

        Mockito.verifyNoInteractions(childRepository)
        Mockito.verifyNoInteractions(historyRepository)
        Mockito.verifyNoInteractions(modeProgressRepository)
        Mockito.verifyNoInteractions(progressRepository)
    }

    @Test
    fun historyCanBeFilteredByGameType() {
        Mockito.`when`(childRepository.existsById(child.id!!)).thenReturn(true)
        Mockito.`when`(
            historyRepository.findAllByChildIdAndGameTypeOrderByPlayedAtDesc(
                child.id!!,
                GameType.HANDWRITING,
            )
        ).thenReturn(emptyList())

        assertTrue(service.getHistory(child.id!!, GameType.HANDWRITING).isEmpty())
        Mockito.verify(historyRepository).findAllByChildIdAndGameTypeOrderByPlayedAtDesc(
            child.id!!,
            GameType.HANDWRITING,
        )
        Mockito.verify(historyRepository, Mockito.never())
            .findAllByChildIdOrderByPlayedAtDesc(child.id!!)
    }

    @Test
    fun missingChildCannotReadProgress() {
        Mockito.`when`(childRepository.existsById(child.id!!)).thenReturn(false)

        assertThrows<ChildNotFoundException> { service.getProgress(child.id!!) }
    }

    @Test
    fun missingModeProgressReturnsZeroValues() {
        Mockito.`when`(childRepository.existsById(child.id!!)).thenReturn(true)
        Mockito.`when`(
            modeProgressRepository.findByChildIdAndGameType(child.id!!, GameType.HANDWRITING)
        ).thenReturn(null)

        val result = service.getModeProgress(child.id!!, GameType.HANDWRITING)

        assertEquals(GameType.HANDWRITING, result.gameType)
        assertEquals(0, result.highestLevel)
        assertEquals(0, result.totalStars)
    }

    private fun request(
        gameType: GameType = GameType.WORD_SCRAMBLE,
        starsEarned: Int = 2,
        completed: Boolean = true,
    ) = GameSessionRequest(
        gameType = gameType,
        level = 1,
        durationSeconds = 60,
        attempts = 1,
        starsEarned = starsEarned,
        completed = completed,
    )

    private fun simulatePersist(history: GameHistory): GameHistory {
        ReflectionTestUtils.setField(history, "id", UUID.randomUUID())
        return history
    }

    private fun assertReplayTotalStars(previousBest: Int, starsEarned: Int, expectedTotal: Int) {
        val modeProgress = GameModeProgress(
            child = child,
            gameType = GameType.WORD_SCRAMBLE,
            highestLevel = 1,
            totalStars = previousBest,
        )
        val globalProgress = GameProgress(
            child = child,
            highestLevel = 1,
            totalStars = previousBest,
        )
        stubPreviousAttempt(previousBest, modeProgress, globalProgress)

        service.recordSession(child.id!!, request(starsEarned = starsEarned))

        assertEquals(expectedTotal, modeProgress.totalStars)
        assertEquals(expectedTotal, globalProgress.totalStars)
        Mockito.verify(modeProgressRepository).save(modeProgress)
        Mockito.verify(progressRepository).save(globalProgress)
    }

    private fun stubPreviousAttempt(
        bestStars: Int,
        modeProgress: GameModeProgress,
        globalProgress: GameProgress,
    ) {
        Mockito.`when`(historyRepository.findBestStars(child.id!!, GameType.WORD_SCRAMBLE, 1))
            .thenReturn(bestStars)
        Mockito.`when`(
            modeProgressRepository.findByChildIdAndGameType(child.id!!, GameType.WORD_SCRAMBLE)
        ).thenReturn(modeProgress)
        Mockito.`when`(progressRepository.findByChildId(child.id!!))
            .thenReturn(globalProgress)
    }
}
