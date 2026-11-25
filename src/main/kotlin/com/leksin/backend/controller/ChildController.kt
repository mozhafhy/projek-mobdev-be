package com.leksin.backend.controller

import com.leksin.backend.dto.request.ChildCreateRequest
import com.leksin.backend.dto.request.ChildProfileUpdateRequest
import com.leksin.backend.dto.response.ChildResponse
import com.leksin.backend.security.TokenPrincipal
import com.leksin.backend.service.ChildService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/parent/children")
@PreAuthorize("hasRole('PARENT')")
class ChildController(
    private val childService: ChildService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal principal: TokenPrincipal,
        @Valid @RequestBody request: ChildCreateRequest,
    ): ChildResponse {
        return childService.create(principal.userId, request)
    }

    @GetMapping
    fun findAll(
        @AuthenticationPrincipal principal: TokenPrincipal,
    ): List<ChildResponse> {
        return childService.findAll(principal.userId)
    }

    @DeleteMapping("/{childId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal principal: TokenPrincipal,
        @PathVariable childId: UUID,
    ) {
        childService.delete(principal.userId, childId)
    }

    @PutMapping("/{childId}")
    fun updateProfile(
        @AuthenticationPrincipal principal: TokenPrincipal,
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
