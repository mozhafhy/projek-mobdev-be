package com.leksin.backend.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChildOtpRequest(
    @field:NotBlank
    val parentUsername: String,

    @field:NotBlank
    @field:Size(min = 2, max = 32)
    val nickname: String,
)
