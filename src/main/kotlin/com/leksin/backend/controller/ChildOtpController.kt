package com.leksin.backend.controller

import com.leksin.backend.dto.response.AuthResponse
import com.leksin.backend.dto.request.ChildOtpRequest
import com.leksin.backend.dto.request.ChildOtpVerifyRequest
import com.leksin.backend.dto.response.MessageResponse
import com.leksin.backend.service.ChildOtpService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/child/auth")
class ChildOtpController(
    private val childOtpService: ChildOtpService,
) {

    @PostMapping("/request-otp")
    fun requestOtp(
        @Valid @RequestBody request: ChildOtpRequest,
    ): ResponseEntity<MessageResponse> {
        childOtpService.requestOtp(request)
        return ResponseEntity.ok(
            MessageResponse("If the account exists, an OTP has been sent")
        )
    }

    @PostMapping("/verify-otp")
    fun verifyOtp(
        @Valid @RequestBody request: ChildOtpVerifyRequest,
    ): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(
            childOtpService.verifyOtp(request)
        )
    }
}
