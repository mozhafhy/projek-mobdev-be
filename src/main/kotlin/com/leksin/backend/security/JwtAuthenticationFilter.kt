package com.leksin.backend.security

import com.leksin.backend.service.TokenRevocationService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val tokenRevocationService: TokenRevocationService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)

        val principal = token?.let(jwtService::parse)

        if (
            principal != null &&
            !tokenRevocationService.isRevoked(principal.tokenId)
        ) {
            val authentication = UsernamePasswordAuthenticationToken(
                principal,
                null,
                listOf(
                    SimpleGrantedAuthority("ROLE_${principal.role.name}")
                ),
            )

            SecurityContextHolder.getContext().authentication =
                authentication
        }

        filterChain.doFilter(request, response)
    }
}
