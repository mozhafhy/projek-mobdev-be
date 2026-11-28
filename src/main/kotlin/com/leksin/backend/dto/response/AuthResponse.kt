package com.leksin.backend.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Authentication result containing the JWT access token.")
data class AuthResponse(
    @field:Schema(description = "Authenticated account identifier.", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    val userId: UUID,

    @field:Schema(description = "Authenticated username or child nickname.", example = "parent01")
    val username: String,

    @field:Schema(description = "Role assigned to the token.", example = "PARENT")
    val role: String,

    @field:Schema(description = "JWT access token. Send it in the Authorization header as a Bearer token.")
    val accessToken: String,

    @field:Schema(description = "Authorization scheme.", example = "Bearer")
    val tokenType: String = "Bearer",

    @field:Schema(description = "Token lifetime in seconds.", example = "86400")
    val expiresIn: Long,
)
