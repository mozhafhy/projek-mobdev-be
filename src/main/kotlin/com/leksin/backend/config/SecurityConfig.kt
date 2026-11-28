package com.leksin.backend.config

import com.leksin.backend.security.JwtAuthenticationFilter
import com.leksin.backend.security.JwtService
import com.leksin.backend.service.TokenRevocationService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtService: JwtService,
        tokenRevocationService: TokenRevocationService,
    ): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                }
            }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/parent/register",
                    "/api/parent/login",
                    "/api/parent/auth/request-otp",
                    "/api/parent/auth/verify-otp",
                    "/api/child/auth/request-otp",
                    "/api/child/auth/verify-otp",
                    "/actuator/health",
                    "/actuator/health/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/error",
                ).permitAll()

                it.anyRequest().authenticated()
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtService, tokenRevocationService),
                UsernamePasswordAuthenticationFilter::class.java,
            )
            .build()
    }
}
