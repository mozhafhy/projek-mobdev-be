package com.leksin.backend.controller

import com.leksin.backend.security.TokenPrincipal
import com.leksin.backend.service.TokenRevocationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authenticated session management.")
class AuthController(
    private val tokenRevocationService: TokenRevocationService,
) {

    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('PARENT', 'CHILD')")
    @Operation(
        summary = "Log out",
        description = "Revokes the JWT used for this request. The token can no longer authenticate subsequent requests.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Logout successful. No response body."),
            ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, expired, or revoked."),
            ApiResponse(responseCode = "403", description = "Authenticated account does not have a permitted role."),
        ]
    )
    fun logout(
        @AuthenticationPrincipal principal: TokenPrincipal,
    ): ResponseEntity<Void> {
        tokenRevocationService.revoke(
            tokenId = principal.tokenId,
            expiresAt = principal.expiresAt,
        )

        return ResponseEntity.noContent().build()
    }
}
