package com.leksin.backend.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Public parent profile data. Passwords and OTP secrets are never included.")
data class ParentResponse(
    @field:Schema(description = "Parent account identifier.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    val id: UUID,

    @field:Schema(description = "Parent username.", example = "parent01")
    val username: String,

    @field:Schema(description = "Registered email address.", example = "parent@example.com")
    val email: String,
)
