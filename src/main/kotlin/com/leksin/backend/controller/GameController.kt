package com.leksin.backend.controller

import com.leksin.backend.dto.request.GameSessionRequest
import com.leksin.backend.dto.response.GameHistoryResponse
import com.leksin.backend.dto.response.GameModeProgressResponse
import com.leksin.backend.dto.response.GameProgressResponse
import com.leksin.backend.entity.GameType
import com.leksin.backend.security.ResourceAuthorizationService
import com.leksin.backend.service.GameService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/child/{childId}/games")
@Tag(name = "Games", description = "Game session records, history, and learning progress for child profiles.")
@SecurityRequirement(name = "bearerAuth")
class GameController(
    private val gameService: GameService,
    private val authorizationService: ResourceAuthorizationService,
) {
    @PostMapping("/sessions")
    @Operation(
        summary = "Record a game session",
        description = "Stores a completed or incomplete game session. Only completed sessions contribute to progress; incomplete sessions must have zero stars.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Game session recorded."),
            ApiResponse(responseCode = "400", description = "Request validation failed or an incomplete session contains stars."),
            ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, expired, or revoked."),
            ApiResponse(responseCode = "403", description = "The authenticated user cannot access this child profile."),
            ApiResponse(responseCode = "404", description = "Child profile does not exist."),
        ]
    )
    fun recordSession(
        @Parameter(description = "UUID of the child profile whose session is being recorded.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        @PathVariable childId: UUID,
        @Valid @RequestBody request: GameSessionRequest,
        authentication: Authentication,
    ): ResponseEntity<GameHistoryResponse> {
        authorizationService.requireChildAccess(childId, authentication)

        return ResponseEntity(
            gameService.recordSession(childId, request),
            HttpStatus.CREATED,
        )
    }

    @GetMapping("/history")
    @Operation(
        summary = "Get game history",
        description = "Returns game sessions for a child, ordered from newest to oldest. The gameType filter is optional.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Game history retrieved. An empty list is returned when no sessions match."),
            ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, expired, or revoked."),
            ApiResponse(responseCode = "403", description = "The authenticated user cannot access this child profile."),
            ApiResponse(responseCode = "404", description = "Child profile does not exist."),
        ]
    )
    fun getHistory(
        @Parameter(description = "UUID of the child profile.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        @PathVariable childId: UUID,
        @Parameter(
            name = "gameType",
            `in` = ParameterIn.QUERY,
            description = "Optional game mode filter.",
            example = "WORD_SCRAMBLE",
        )
        @RequestParam(required = false) gameType: GameType?,
        authentication: Authentication,
    ): ResponseEntity<List<GameHistoryResponse>> {
        authorizationService.requireChildAccess(childId, authentication)

        return ResponseEntity.ok(gameService.getHistory(childId, gameType))
    }

    @GetMapping("/progress")
    @Operation(
        summary = "Get overall game progress",
        description = "Returns aggregated progress across all game modes. A child without recorded progress receives zero-valued progress fields.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Overall progress retrieved."),
            ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, expired, or revoked."),
            ApiResponse(responseCode = "403", description = "The authenticated user cannot access this child profile."),
            ApiResponse(responseCode = "404", description = "Child profile does not exist."),
        ]
    )
    fun getProgress(
        @Parameter(description = "UUID of the child profile.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        @PathVariable childId: UUID,
        authentication: Authentication,
    ): ResponseEntity<GameProgressResponse> {
        authorizationService.requireChildAccess(childId, authentication)

        return ResponseEntity.ok(gameService.getProgress(childId))
    }

    @GetMapping("/progress/{gameType}")
    @Operation(
        summary = "Get progress for a game mode",
        description = "Returns progress for one game mode. A child without progress in the selected mode receives zero-valued level and star totals.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Game mode progress retrieved."),
            ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, expired, or revoked."),
            ApiResponse(responseCode = "403", description = "The authenticated user cannot access this child profile."),
            ApiResponse(responseCode = "404", description = "Child profile does not exist."),
        ]
    )
    fun getModeProgress(
        @Parameter(description = "UUID of the child profile.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        @PathVariable childId: UUID,
        @Parameter(description = "Game mode: WORD_SCRAMBLE or HANDWRITING.", example = "HANDWRITING")
        @PathVariable gameType: GameType,
        authentication: Authentication,
    ): ResponseEntity<GameModeProgressResponse> {
        authorizationService.requireChildAccess(childId, authentication)

        return ResponseEntity.ok(gameService.getModeProgress(childId, gameType))
    }
}
