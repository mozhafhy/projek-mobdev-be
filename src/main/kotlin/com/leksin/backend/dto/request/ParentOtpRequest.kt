package com.leksin.backend.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Request a verification OTP for a parent account.")
data class ParentOtpRequest(
    @field:NotBlank
    @field:Size(min = 4, max = 50)
    @field:Schema(
        description = "Username of the parent account.",
        example = "parent01",
        minLength = 4,
        maxLength = 50,
    )
    val username: String,
)
