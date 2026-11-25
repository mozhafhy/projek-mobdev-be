package com.leksin.backend.controller

import com.leksin.backend.dto.GameHistoryResponse
import com.leksin.backend.dto.GameProgressResponse
import com.leksin.backend.dto.GameSessionRequest
import com.leksin.backend.security.ResourceAuthorizationService
import com.leksin.backend.service.GameService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/child/{childId}/games")
class GameController(
    private val gameService: GameService,
    private val authorizationService: ResourceAuthorizationService,
) {
    @PostMapping("/sessions")
    fun recordSession(
        @PathVariable childId: UUID,
        @Valid @RequestBody request: GameSessionRequest,
        authentication: Authentication
    ): ResponseEntity<GameHistoryResponse> {
        authorizationService.requireChildAccess(
            childId,
            authentication,
        )

        return ResponseEntity(
            gameService.recordSession(childId, request),
            HttpStatus.CREATED
        )
    }

    @GetMapping("/history")
    fun getHistory(
        @PathVariable childId: UUID,
        authentication: Authentication
    ): ResponseEntity<List<GameHistoryResponse>> {
        authorizationService.requireChildAccess(
            childId,
            authentication,
        )

        return ResponseEntity.ok(
        gameService.getHistory(childId)
        )
    }

    @GetMapping("/progress")
    fun getProgress(
        @PathVariable childId: UUID,
        authentication: Authentication
    ): ResponseEntity<GameProgressResponse> {
        authorizationService.requireChildAccess(
            childId,
            authentication,
        )

        return ResponseEntity.ok(
        gameService.getProgress(childId)
        )
    }
}
