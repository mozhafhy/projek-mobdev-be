package com.leksin.backend.controller

import com.leksin.backend.dto.request.ParentOtpRequest
import com.leksin.backend.dto.request.ParentOtpVerifyRequest
import com.leksin.backend.dto.response.OtpMessageResponse
import com.leksin.backend.service.ParentOtpService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/parent/auth")
class ParentOtpController(
    private val parentOtpService: ParentOtpService,
) {

    @PostMapping("/request-otp")
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
    fun verifyOtp(
        @Valid @RequestBody request: ParentOtpVerifyRequest,
    ): ResponseEntity<OtpMessageResponse> {
        parentOtpService.verifyOtp(request)

        return ResponseEntity.ok(
            OtpMessageResponse("Email verified successfully")
        )
    }
}