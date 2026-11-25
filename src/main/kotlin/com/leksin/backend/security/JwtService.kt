package com.leksin.backend.security

import com.leksin.backend.entity.Role
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value($$"${app.jwt.secret}") secret: String,
    @Value($$"${app.jwt.expiration-ms}") private val expirationMs: Long,
    @Value($$"${app.jwt.issuer}") private val issuer: String,
) {
    val expirationSeconds: Long
        get() = expirationMs / 1000

    private val signingKey: SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))

    private val parser = Jwts.parser()
        .verifyWith(signingKey)
        .requireIssuer(issuer)
        .build()

    private val logger = LoggerFactory.getLogger(JwtService::class.java)

    fun generateToken(userId: UUID, role: Role): String {
        val now = Date()

        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .issuer(issuer)
            .subject(userId.toString())
            .claim(ROLE_CLAIM, role.name)
            .issuedAt(now)
            .expiration(Date(now.time + expirationMs))
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact()
    }

    fun parse(token: String): TokenPrincipal? =
        try {
            toPrincipal(parser.parseSignedClaims(token).payload)
        } catch (e: JwtException) {
            logRejection(e)
        } catch (e: IllegalArgumentException) {
            logRejection(e)
        }

    private fun toPrincipal(claims: Claims) = TokenPrincipal(
        userId = UUID.fromString(claims.subject),
        role = Role.valueOf(requireNotNull(claims.get(ROLE_CLAIM, String::class.java))),
        tokenId = UUID.fromString(requireNotNull(claims.id)),
        expiresAt = requireNotNull(claims.expiration).toInstant(),
    )

    private fun logRejection(e: Exception): TokenPrincipal? {
        logger.warn("Token ditolak: {}", e.javaClass.simpleName)
        return null
    }

    private companion object {
        const val ROLE_CLAIM = "role"
    }
}
