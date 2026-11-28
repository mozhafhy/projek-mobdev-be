package com.leksin.backend.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Data required to create a parent account.")
data class ParentRegisterRequest(
    @field:NotBlank
    @field:Size(min = 4, max = 50)
    @field:Schema(description = "Unique parent username.", example = "parent01", minLength = 4, maxLength = 50)
    val username: String,

    @field:NotBlank
    @field:Size(min = 8, max = 72)
    @field:Schema(description = "Account password. Must contain 8 to 72 characters.", example = "StrongPass123", minLength = 8, maxLength = 72)
    val password: String,

    @field:NotBlank
    @field:Email
    @field:Schema(description = "Email address used for OTP verification.", example = "parent@example.com", format = "email")
    val email: String,
)
