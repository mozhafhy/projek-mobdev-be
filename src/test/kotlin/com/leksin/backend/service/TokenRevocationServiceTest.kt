package com.leksin.backend.service

import com.leksin.backend.repository.RevokedTokenRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class TokenRevocationServiceTest {

    @Mock
    private lateinit var revokedTokenRepository: RevokedTokenRepository

    private lateinit var service: TokenRevocationService

    @BeforeEach
    fun setUp() {
        service = TokenRevocationService(revokedTokenRepository)
    }

    @Test
    fun `revoke inserts token when called`() {
        val tokenId = UUID.randomUUID()
        val expiresAt = Instant.now().plusSeconds(3600)

        service.revoke(tokenId, expiresAt)

        val invocation = Mockito.mockingDetails(revokedTokenRepository)
            .invocations
            .single()

        assertEquals("insertIfAbsent", invocation.method.name)
        assertEquals(tokenId, invocation.arguments[0])
        assertEquals(expiresAt, invocation.arguments[1])
        assertTrue(invocation.arguments[2] is Instant)
    }

    @Test
    fun `isRevoked returns true when token exists`() {
        val tokenId = UUID.randomUUID()

        Mockito.`when`(revokedTokenRepository.existsById(tokenId))
            .thenReturn(true)

        assertTrue(service.isRevoked(tokenId))
    }

    @Test
    fun `isRevoked returns false when token does not exist`() {
        val tokenId = UUID.randomUUID()

        Mockito.`when`(revokedTokenRepository.existsById(tokenId))
            .thenReturn(false)

        assertFalse(service.isRevoked(tokenId))
    }
}