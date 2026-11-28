package com.leksin.backend.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

@Schema(description = "Fields that can be changed on the authenticated parent profile. Both fields are optional.")
data class ParentProfileUpdateRequest(
    @field:Size(min = 3, max = 50)
    @field:Schema(description = "New username, if it should be changed.", example = "parent_new", minLength = 3, maxLength = 50, nullable = true)
    val username: String? = null,

    @field:Email
    @field:Schema(description = "New email address. Changing it triggers a new email verification OTP.", example = "new-parent@example.com", format = "email", nullable = true)
    val email: String? = null,
)
