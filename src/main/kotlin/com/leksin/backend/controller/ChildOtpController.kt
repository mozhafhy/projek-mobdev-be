package com.leksin.backend.controller

import com.leksin.backend.dto.request.ChildOtpRequest
import com.leksin.backend.dto.request.ChildOtpVerifyRequest
import com.leksin.backend.dto.response.AuthResponse
import com.leksin.backend.dto.response.OtpMessageResponse
import com.leksin.backend.service.ChildOtpService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/child/auth")
@Tag(name = "Child Authentication", description = "OTP-based authentication for child accounts.")
class ChildOtpController(
    private val childOtpService: ChildOtpService,
) {

    @PostMapping("/request-otp")
    @Operation(
        summary = "Request child login OTP",
        description = "Sends a six-digit login code to the email address of the parent account associated with the supplied child nickname. The response is generic when the parent-child combination is not found.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Request processed. A generic message is returned."),
            ApiResponse(responseCode = "400", description = "Request validation failed."),
            ApiResponse(responseCode = "500", description = "The OTP email could not be sent."),
        ]
    )
    fun requestOtp(
        @Valid @RequestBody request: ChildOtpRequest,
    ): ResponseEntity<OtpMessageResponse> {
        childOtpService.requestOtp(request)
        return ResponseEntity.ok(
            OtpMessageResponse("If the account exists, an OTP has been sent")
        )
    }

    @PostMapping("/verify-otp")
    @Operation(
        summary = "Verify child login OTP",
        description = "Verifies the six-digit code for the child identified by parent username and nickname. On success, returns a JWT access token with the CHILD role.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OTP verified. Child authentication token returned."),
            ApiResponse(responseCode = "400", description = "Request validation failed. The code must contain exactly six digits."),
            ApiResponse(responseCode = "401", description = "Child identity or OTP is invalid, expired, already used, or has exceeded the failed-attempt limit."),
        ]
    )
    fun verifyOtp(
        @Valid @RequestBody request: ChildOtpVerifyRequest,
    ): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(
            childOtpService.verifyOtp(request)
        )
    }
}
