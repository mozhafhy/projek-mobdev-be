package com.leksin.backend.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Child profile data.")
data class ChildResponse(
    @field:Schema(description = "Child profile identifier.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    val id: UUID,

    @field:Schema(description = "Child nickname.", example = "Bintang")
    val nickname: String,
)
