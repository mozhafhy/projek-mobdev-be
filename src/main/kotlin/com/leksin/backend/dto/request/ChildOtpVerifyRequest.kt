package com.leksin.backend.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Verifies a child login request using the OTP sent to the parent's email.")
data class ChildOtpVerifyRequest(
    @field:NotBlank
    @field:Schema(description = "Username of the parent account associated with the child.", example = "parent01")
    val parentUsername: String,

    @field:NotBlank
    @field:Size(min = 2, max = 32)
    @field:Schema(description = "Child nickname registered under the parent account.", example = "Bintang", minLength = 2, maxLength = 32)
    val nickname: String,

    @field:NotBlank
    @field:Pattern(regexp = "^\\d{6}$")
    @field:Schema(description = "Six-digit OTP. Send it as a string to preserve leading zeroes.", example = "012345", minLength = 6, maxLength = 6, pattern = "^\\d{6}$")
    val code: String,
)
