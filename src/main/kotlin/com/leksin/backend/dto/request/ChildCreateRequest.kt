package com.leksin.backend.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Data required to create a child profile under the authenticated parent.")
data class ChildCreateRequest(
    @field:NotBlank
    @field:Size(min = 2, max = 32)
    @field:Schema(description = "Child nickname. Must be unique under the parent account.", example = "Bintang", minLength = 2, maxLength = 32)
    val nickname: String,
)
