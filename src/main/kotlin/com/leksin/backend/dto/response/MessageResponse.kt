package com.leksin.backend.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Generic response returned by OTP request and verification endpoints.")
data class OtpMessageResponse(
    @field:Schema(
        description = "Human-readable result message.",
        example = "Email verified successfully",
    )
    val message: String
)
