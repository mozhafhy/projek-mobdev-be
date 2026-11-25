package com.leksin.backend.controller

import com.leksin.backend.dto.ChildCreateRequest
import com.leksin.backend.dto.ChildResponse
import com.leksin.backend.security.ResourceAuthorizationService
import com.leksin.backend.security.TokenPrincipal
import com.leksin.backend.service.ChildService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/parent/children")
class ChildController(
    private val childService: ChildService,
    private val authorizationService: ResourceAuthorizationService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal principal: TokenPrincipal,
        @Valid @RequestBody request: ChildCreateRequest,
        authentication: Authentication,
    ): ChildResponse {
        authorizationService.requireParentAccess(principal.userId, authentication)
        return childService.create(principal.userId, request)
    }

    @GetMapping
    fun findAll(
        @AuthenticationPrincipal principal: TokenPrincipal,
        authentication: Authentication,
    ): List<ChildResponse> {
        authorizationService.requireParentAccess(principal.userId, authentication)
        return childService.findAll(principal.userId)
    }

    @DeleteMapping("/{childId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal principal: TokenPrincipal,
        @PathVariable childId: UUID,
        authentication: Authentication,
    ) {
        val parentId = principal.userId
        authorizationService.requireParentAccess(
            parentId,
            authentication
        )

        childService.delete(parentId, childId)
    }
}
