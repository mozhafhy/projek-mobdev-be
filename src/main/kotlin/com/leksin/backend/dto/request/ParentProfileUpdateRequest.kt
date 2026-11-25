package com.leksin.backend.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class ParentProfileUpdateRequest(
    @field:Size(min = 3, max = 50)
    val username: String? = null,

    @field:Email
    val email: String? = null,
)
