package com.leksin.backend.config

import com.leksin.backend.security.IpRateLimitInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class RateLimitConfig(
    private val ipRateLimitInterceptor: IpRateLimitInterceptor,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(ipRateLimitInterceptor)
            .addPathPatterns(
                "/api/parent/register",
                "/api/parent/auth/request-otp",
                "/api/parent/auth/verify-otp",
                "/api/child/auth/request-otp",
                "/api/child/auth/verify-otp",
            )
    }
}
