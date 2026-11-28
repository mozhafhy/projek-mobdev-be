package com.leksin.backend.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ParentOtpVerifyRequest(
    @field:NotBlank
    @field:Size(min = 4, max = 50)
    val username: String,

    @field:NotBlank
    @field:Pattern(regexp = "^\\d{6}$")
    val code: String,
)
