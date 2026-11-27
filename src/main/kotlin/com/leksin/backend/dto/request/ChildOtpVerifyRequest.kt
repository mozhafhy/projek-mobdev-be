package com.leksin.backend.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ChildOtpVerifyRequest(
    @field:NotBlank
    val parentUsername: String,

    @field:NotBlank
    @field:Size(min = 2, max = 32)
    val nickname: String,

    @field:NotBlank
    @field:Pattern(regexp = "^\\d{6}$")
    val code: String,
)
