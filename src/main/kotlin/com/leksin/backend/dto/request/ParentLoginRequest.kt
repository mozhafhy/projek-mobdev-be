package com.leksin.backend.dto.request

import jakarta.validation.constraints.NotBlank

data class ParentLoginRequest(
    @field:NotBlank
    val username: String,

    @field:NotBlank
    val password: String,
)