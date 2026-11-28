package com.leksin.backend.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "LEKSIN Backend API",
        version = "1.0.0",
        description = "REST API for parent and child accounts, OTP authentication, and learning game progress.",
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Enter the JWT access token returned by a successful parent login or child OTP verification.",
)
class OpenApiConfig
