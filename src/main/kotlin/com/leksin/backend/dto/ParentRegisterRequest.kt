package com.leksin.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ParentRegisterRequest(
    @field:NotBlank
    @field:Size(min = 4, max = 50)
    val username: String,

    @field:NotBlank
    @field:Size(min = 8, max = 72)
    val password: String,

    @field:NotBlank
    @field:Email
    val email: String,
)
