package com.leksin.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChildCreateRequest(
    @field:NotBlank
    @field:Size(min = 2, max = 32)
    val nickname: String,
)
