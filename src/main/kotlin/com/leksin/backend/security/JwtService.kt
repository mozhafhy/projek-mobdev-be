package com.leksin.backend.security

import com.leksin.backend.entity.Role
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value($$"${app.jwt.secret}") secret: String,
    @Value($$"${app.jwt.expiration-ms}") private val expirationMs: Long,
) {
    val expirationSeconds: Long
        get() = expirationMs / 1000

    private val signingKey: SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))

    fun generateToken(userId: UUID, role: Role): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .claim(ROLE_CLAIM, role.name)
            .issuedAt(now)
            .expiration(Date(now.time + expirationMs))
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact()
    }

    fun parse(token: String): TokenPrincipal? =
        try {
            val claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload

            val roleClaim = claims.get(ROLE_CLAIM, String::class.java)

            TokenPrincipal(
                userId = UUID.fromString(claims.subject),
                role = Role.valueOf(roleClaim),
            )
        } catch (e: JwtException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }

    private companion object {
        const val ROLE_CLAIM = "role"
    }
}
