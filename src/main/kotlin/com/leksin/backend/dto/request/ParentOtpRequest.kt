package com.leksin.backend.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ParentOtpRequest(
    @field:NotBlank
    @field:Size(min = 4, max = 50)
    val username: String,
)
