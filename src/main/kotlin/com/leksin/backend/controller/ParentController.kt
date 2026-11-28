package com.leksin.backend.controller

import com.leksin.backend.dto.request.ParentLoginRequest
import com.leksin.backend.dto.request.ParentProfileUpdateRequest
import com.leksin.backend.dto.request.ParentRegisterRequest
import com.leksin.backend.dto.response.AuthResponse
import com.leksin.backend.dto.response.ParentResponse
import com.leksin.backend.security.ResourceAuthorizationService
import com.leksin.backend.security.TokenPrincipal
import com.leksin.backend.service.ParentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/parent")
@Tag(name = "Parent Accounts", description = "Parent registration, authentication, and profile management.")
class ParentController(
    private val parentService: ParentService,
    private val authorizationService: ResourceAuthorizationService,
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register parent account",
        description = "Creates a parent account and automatically sends an email verification OTP to the registered email address. The account cannot log in until its email is verified through POST /api/parent/auth/verify-otp.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Parent account created. Email verification is pending."),
            ApiResponse(responseCode = "400", description = "Request validation failed."),
            ApiResponse(responseCode = "409", description = "Username or email is already registered."),
            ApiResponse(responseCode = "500", description = "The verification email could not be sent."),
        ]
    )
    fun register(
        @Valid @RequestBody request: ParentRegisterRequest,
    ): ResponseEntity<ParentResponse> {
        val result = parentService.register(request)
        return ResponseEntity.created(URI("/${result.id}")).body(result)
    }

    @PostMapping("/login")
    @Operation(
        summary = "Log in as parent",
        description = "Authenticates a verified parent account and returns a JWT access token. Email verification must be completed before login.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Login successful. JWT access token returned."),
            ApiResponse(responseCode = "400", description = "Request validation failed."),
            ApiResponse(responseCode = "401", description = "Credentials are invalid or the email address has not been verified."),
        ]
    )
    fun login(
        @Valid @RequestBody request: ParentLoginRequest,
    ): ResponseEntity<AuthResponse> {
        val result = parentService.login(request)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{username}")
    @Operation(
        summary = "Get parent profile",
        description = "Returns the profile associated with the given username. Access is limited to the authenticated parent who owns that profile.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Parent profile retrieved."),
            ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, expired, or revoked."),
            ApiResponse(responseCode = "403", description = "The authenticated user does not own the requested profile."),
            ApiResponse(responseCode = "404", description = "Parent profile does not exist."),
        ]
    )
    fun getProfile(
        @Parameter(description = "Username of the parent account.", example = "parent01")
        @PathVariable username: String,
        authentication: Authentication,
    ): ResponseEntity<ParentResponse> {
        authorizationService.requireOwnParentProfile(
            username = username,
            authentication = authentication,
        )

        val result = parentService.findByUsername(username)
        return ResponseEntity.ok(result)
    }

    @PreAuthorize("hasRole('PARENT')")
    @PatchMapping("/profile")
    @Operation(
        summary = "Update parent profile",
        description = "Updates the authenticated parent's username, email, or both. If the email changes, it is marked unverified and a new verification OTP is sent; the parent must verify the new email before logging in again.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Parent profile updated."),
            ApiResponse(responseCode = "400", description = "Request validation failed."),
            ApiResponse(responseCode = "401", description = "Bearer token is missing, invalid, expired, or revoked."),
            ApiResponse(responseCode = "403", description = "The authenticated account is not a parent."),
            ApiResponse(responseCode = "404", description = "Parent profile does not exist."),
            ApiResponse(responseCode = "409", description = "Username or email is already used by another account."),
            ApiResponse(responseCode = "500", description = "The verification email could not be sent after an email change."),
        ]
    )
    fun updateProfile(
        @AuthenticationPrincipal principal: TokenPrincipal,
        @Valid @RequestBody request: ParentProfileUpdateRequest,
    ): ResponseEntity<ParentResponse> {
        val result = parentService.updateProfile(
            parentId = principal.userId,
            request = request,
        )
        return ResponseEntity.ok(result)
    }
}
