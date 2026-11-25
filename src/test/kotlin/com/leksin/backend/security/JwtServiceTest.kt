package com.leksin.backend.security

import com.leksin.backend.entity.Role
import java.util.Base64
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class JwtServiceTest {
    private val secret = Base64.getEncoder().encodeToString(ByteArray(32) { it.toByte() })

    @Test
    fun `generated token preserves user id and role`() {
        val service = JwtService(secret, 60_000)
        val userId = UUID.randomUUID()

        val principal = service.parse(service.generateToken(userId, Role.PARENT))

        assertNotNull(principal)
        assertEquals(userId, principal.userId)
        assertEquals(Role.PARENT, principal.role)
    }

    @Test
    fun `expired token is rejected`() {
        val service = JwtService(secret, -1)
        val token = service.generateToken(UUID.randomUUID(), Role.CHILD)

        assertNull(service.parse(token))
    }

    @Test
    fun `malformed token is rejected`() {
        val service = JwtService(secret, 60_000)

        assertNull(service.parse("not-a-jwt"))
    }
}
