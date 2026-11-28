package com.leksin.backend.controller

import com.leksin.backend.dto.request.ParentOtpRequest
import com.leksin.backend.dto.request.ParentOtpVerifyRequest
import com.leksin.backend.dto.response.OtpMessageResponse
import com.leksin.backend.service.ParentOtpService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/parent/auth")
@Tag(
    name = "Parent OTP",
    description = "Email verification endpoints for parent accounts."
)
class ParentOtpController(
    private val parentOtpService: ParentOtpService,
) {

    @PostMapping("/request-otp")
    @Operation(
        summary = "Request parent email verification OTP",
        description = """
            Sends a six-digit verification code to the email address registered
            to the parent account. Registration sends an OTP automatically;
            this endpoint can be used to request another code.

            The response intentionally does not reveal whether the username
            exists or whether the email has already been verified.
            The OTP expires after 2 minutes.
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Request processed. The response is intentionally generic.",
                content = [
                    Content(
                        schema = Schema(implementation = OtpMessageResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Request validation failed."
            ),
            ApiResponse(
                responseCode = "500",
                description = "The verification email could not be sent."
            )
        ]
    )
    fun requestOtp(
        @Valid @RequestBody request: ParentOtpRequest,
    ): ResponseEntity<OtpMessageResponse> {
        parentOtpService.requestOtp(request.username)

        return ResponseEntity.ok(
            OtpMessageResponse(
                "If the account exists and requires verification, an OTP has been sent"
            )
        )
    }

    @PostMapping("/verify-otp")
    @Operation(
        summary = "Verify parent email using OTP",
        description = """
            Verifies the parent account using the six-digit code sent by email.
            The code is valid for 2 minutes and allows at most 3 failed attempts.
            A successful verification marks the parent's email as verified,
            allowing the parent to log in.
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Email verified successfully.",
                content = [
                    Content(
                        schema = Schema(implementation = OtpMessageResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Request validation failed. The code must contain exactly 6 digits."
            ),
            ApiResponse(
                responseCode = "401",
                description = "Username or OTP is invalid, expired, already used, or has exceeded the failed-attempt limit."
            )
        ]
    )
    fun verifyOtp(
        @Valid @RequestBody request: ParentOtpVerifyRequest,
    ): ResponseEntity<OtpMessageResponse> {
        parentOtpService.verifyOtp(request)

        return ResponseEntity.ok(
            OtpMessageResponse("Email verified successfully")
        )
    }
}
