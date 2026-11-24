package com.leksin.backend.controller

import com.leksin.backend.dto.GameHistoryResponse
import com.leksin.backend.dto.GameProgressResponse
import com.leksin.backend.dto.GameSessionRequest
import com.leksin.backend.service.GameService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/child/{childId}/games")
class GameController(
    private val gameService: GameService,
) {
    @PostMapping("/sessions")
    fun recordSession(
        @PathVariable childId: UUID,
        @Valid @RequestBody request: GameSessionRequest,
    ): ResponseEntity<GameHistoryResponse> {
        return ResponseEntity(
            gameService.recordSession(childId, request),
            HttpStatus.CREATED
        )
    }

    @GetMapping("/history")
    fun getHistory(
        @PathVariable childId: UUID,
    ): ResponseEntity<List<GameHistoryResponse>> {
        return ResponseEntity.ok(
        gameService.getHistory(childId)
        )
    }

    @GetMapping("/progress")
    fun getProgress(
        @PathVariable childId: UUID,
    ): ResponseEntity<GameProgressResponse> {
        return ResponseEntity.ok(
        gameService.getProgress(childId)
        )
    }
}
