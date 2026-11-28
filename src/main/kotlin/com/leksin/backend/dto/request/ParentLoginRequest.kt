package com.leksin.backend.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Credentials for parent account login.")
data class ParentLoginRequest(
    @field:NotBlank
    @field:Schema(description = "Registered parent username.", example = "parent01")
    val username: String,

    @field:NotBlank
    @field:Schema(description = "Parent account password.", example = "StrongPass123", format = "password")
    val password: String,
)
