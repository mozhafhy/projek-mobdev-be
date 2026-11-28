package com.leksin.backend.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Identifies a child account to request a login OTP.")
data class ChildOtpRequest(
    @field:NotBlank
    @field:Schema(description = "Username of the parent account associated with the child.", example = "parent01")
    val parentUsername: String,

    @field:NotBlank
    @field:Size(min = 2, max = 32)
    @field:Schema(description = "Child nickname registered under the parent account.", example = "Bintang", minLength = 2, maxLength = 32)
    val nickname: String,
)
