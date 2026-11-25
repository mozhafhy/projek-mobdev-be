package com.leksin.backend.security

import com.leksin.backend.entity.Role
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JwtServiceTest {
    private val secret = Base64.getEncoder().encodeToString(ByteArray(32) { it.toByte() })
    private val signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))

    private fun createService(
        expirationMs: Long = 60_000,
        issuer: String = ISSUER,
    ) = JwtService(secret, expirationMs, issuer)

    @Test
    fun `generated token preserves user id and role`() {
        val service = createService()
        val userId = UUID.randomUUID()

        val principal = service.parse(service.generateToken(userId, Role.PARENT))

        assertNotNull(principal)
        assertEquals(userId, principal.userId)
        assertEquals(Role.PARENT, principal.role)
    }

    @Test
    fun `expired token is rejected`() {
        val service = createService(expirationMs = -1)
        val token = service.generateToken(UUID.randomUUID(), Role.CHILD)

        assertNull(service.parse(token))
    }

    @Test
    fun `malformed token is rejected`() {
        assertNull(createService().parse("not-a-jwt"))
    }

    @Test
    fun `token from another issuer is rejected`() {
        val foreignToken = createService(issuer = "other-service")
            .generateToken(UUID.randomUUID(), Role.PARENT)

        assertNull(createService().parse(foreignToken))
    }

    private fun signedToken(
        subject: String? = UUID.randomUUID().toString(),
        id: String? = UUID.randomUUID().toString(),
        role: String? = Role.PARENT.name,
    ): String = Jwts.builder()
        .id(id)
        .issuer(ISSUER)
        .subject(subject)
        .claim("role", role)
        .expiration(Date(System.currentTimeMillis() + 60_000))
        .signWith(signingKey, Jwts.SIG.HS256)
        .compact()

    @Test
    fun `token without role claim is rejected`() {
        assertNull(createService().parse(signedToken(role = null)))
    }

    @Test
    fun `token with unknown role is rejected`() {
        assertNull(createService().parse(signedToken(role = "NOT_A_ROLE")))
    }

    @Test
    fun `token with non-uuid subject is rejected`() {
        assertNull(createService().parse(signedToken(subject = "bukan-uuid")))
    }

    @Test
    fun `token without token id is rejected`() {
        assertNull(createService().parse(signedToken(id = null)))
    }

    @Test
    fun `token signed with another secret is rejected`() {
        val otherSecret = Base64.getEncoder().encodeToString(ByteArray(32) { (it + 1).toByte() })
        val foreignToken = JwtService(otherSecret, 60_000, ISSUER)
            .generateToken(UUID.randomUUID(), Role.PARENT)

        assertNull(createService().parse(foreignToken))
    }

    @Test
    fun `unsigned token is rejected`() {
        val unsigned = Jwts.builder()
            .id(UUID.randomUUID().toString())
            .issuer(ISSUER)
            .subject(UUID.randomUUID().toString())
            .claim("role", Role.PARENT.name)
            .compact()

        assertNull(createService().parse(unsigned))
    }

    @Test
    fun `each generated token gets a unique token id`() {
        val service = createService()
        val userId = UUID.randomUUID()

        val first = service.parse(service.generateToken(userId, Role.PARENT))
        val second = service.parse(service.generateToken(userId, Role.PARENT))

        assertNotEquals(first?.tokenId, second?.tokenId)
    }

    @Test
    fun `parsed expiration matches generated token expiration`() {
        val service = createService()
        val token = service.generateToken(UUID.randomUUID(), Role.PARENT)

        val principal = service.parse(token)

        assertNotNull(principal)

        val claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload

        assertEquals(
            claims.expiration.toInstant(),
            principal.expiresAt,
        )
    }

    private companion object {
        const val ISSUER = "leksin-backend-test"
    }
}
