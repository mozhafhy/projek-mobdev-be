package com.leksin.backend.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Replacement child profile nickname.")
data class ChildProfileUpdateRequest(
    @field:NotBlank
    @field:Size(min = 2, max = 32)
    @field:Schema(description = "New child nickname.", example = "Bintang Baru", minLength = 2, maxLength = 32)
    val nickname: String,
)
