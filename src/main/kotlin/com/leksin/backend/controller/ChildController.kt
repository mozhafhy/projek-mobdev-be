package com.leksin.backend.controller

import com.leksin.backend.dto.request.ChildCreateRequest
import com.leksin.backend.dto.request.ChildProfileUpdateRequest
import com.leksin.backend.dto.response.ChildResponse
import com.leksin.backend.security.TokenPrincipal
import com.leksin.backend.service.ChildService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/parent/children")
@PreAuthorize("hasRole('PARENT')")
@Tag(name = "Child Profiles", description = "Parent operations for managing child profiles.")
@SecurityRequirement(name = "bearerAuth")
class ChildController(
    private val childService: ChildService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a child profile",
        description = "Creates a child profile under the authenticated parent account. Nicknames must be unique for that parent.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Child profile created."),
            ApiResponse(responseCode = "400", description = "Nickname is missing or outside the allowed length."),
            ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, expired, or revoked."),
            ApiResponse(responseCode = "403", description = "The authenticated account is not a parent."),
            ApiResponse(responseCode = "409", description = "A child with the same nickname already exists under this parent."),
        ]
    )
    fun create(
        @AuthenticationPrincipal principal: TokenPrincipal,
        @Valid @RequestBody request: ChildCreateRequest,
    ): ChildResponse {
        return childService.create(principal.userId, request)
    }

    @GetMapping
    @Operation(
        summary = "List child profiles",
        description = "Returns all child profiles owned by the authenticated parent. An empty list is returned when no child profiles exist.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Child profiles retrieved."),
            ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, expired, or revoked."),
            ApiResponse(responseCode = "403", description = "The authenticated account is not a parent."),
        ]
    )
    fun findAll(
        @AuthenticationPrincipal principal: TokenPrincipal,
    ): List<ChildResponse> {
        return childService.findAll(principal.userId)
    }

    @DeleteMapping("/{childId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete a child profile",
        description = "Deletes a child profile owned by the authenticated parent, including its OTP records, game history, and progress data.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Child profile and associated data deleted."),
            ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, expired, or revoked."),
            ApiResponse(responseCode = "403", description = "The authenticated account is not a parent."),
            ApiResponse(responseCode = "404", description = "Child profile does not exist or is not owned by this parent."),
        ]
    )
    fun delete(
        @AuthenticationPrincipal principal: TokenPrincipal,
        @Parameter(description = "UUID of the child profile.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        @PathVariable childId: UUID,
    ) {
        childService.delete(principal.userId, childId)
    }

    @PutMapping("/{childId}")
    @Operation(
        summary = "Update a child profile",
        description = "Replaces the child's profile nickname. The child must belong to the authenticated parent.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Child profile updated."),
            ApiResponse(responseCode = "400", description = "Nickname is missing or outside the allowed length."),
            ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, expired, or revoked."),
            ApiResponse(responseCode = "403", description = "The authenticated account is not a parent."),
            ApiResponse(responseCode = "404", description = "Child profile does not exist or is not owned by this parent."),
            ApiResponse(responseCode = "409", description = "Another child under this parent already uses the nickname."),
        ]
    )
    fun updateProfile(
        @AuthenticationPrincipal principal: TokenPrincipal,
        @Parameter(description = "UUID of the child profile.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
        @PathVariable childId: UUID,
        @Valid @RequestBody request: ChildProfileUpdateRequest,
    ): ChildResponse {
        return childService.updateProfile(
            parentId = principal.userId,
            childId = childId,
            request = request,
        )
    }
}
