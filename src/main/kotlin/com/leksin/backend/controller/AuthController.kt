package com.leksin.backend.controller

import com.leksin.backend.security.TokenPrincipal
import com.leksin.backend.service.TokenRevocationService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val tokenRevocationService: TokenRevocationService,
) {

    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('PARENT', 'CHILD')")
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
