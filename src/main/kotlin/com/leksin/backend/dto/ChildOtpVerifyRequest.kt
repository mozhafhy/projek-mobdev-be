package com.leksin.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ChildOtpVerifyRequest(
    @field:NotBlank
    val parentUsername: String,

    @field:NotBlank
    @field:Size(min = 2, max = 32)
    val nickname: String,

    @field:Pattern(regexp = "^\\d{4}$")
    val code: String,
)
