package com.leksin.backend.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Verify a parent account using the OTP sent to its registered email.")
data class ParentOtpVerifyRequest(
    @field:NotBlank
    @field:Size(min = 4, max = 50)
    @field:Schema(
        description = "Username of the parent account being verified.",
        example = "parent01",
        minLength = 4,
        maxLength = 50,
    )
    val username: String,

    @field:NotBlank
    @field:Pattern(regexp = "^\\d{6}$")
    @field:Schema(
        description = "Six-digit verification code. Send as a string to preserve leading zeroes.",
        example = "012345",
        minLength = 6,
        maxLength = 6,
    )
    val code: String,
)
