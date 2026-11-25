package com.leksin.backend.security

import com.leksin.backend.entity.Role
import com.leksin.backend.service.TokenRevocationService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class JwtAuthenticationFilterTest {

    @Mock
    private lateinit var jwtService: JwtService

    @Mock
    private lateinit var tokenRevocationService: TokenRevocationService

    private lateinit var filter: JwtAuthenticationFilter

    @BeforeEach
    fun setUp() {
        filter = JwtAuthenticationFilter(
            jwtService,
            tokenRevocationService,
        )
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `valid token establishes authentication`() {
        val token = "valid-token"
        val principal = createPrincipal()

        Mockito.`when`(jwtService.parse(token)).thenReturn(principal)
        Mockito.`when`(
            tokenRevocationService.isRevoked(principal.tokenId)
        ).thenReturn(false)

        val request = createRequest(token)
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        val authentication =
            SecurityContextHolder.getContext().authentication

        assertTrue(authentication is UsernamePasswordAuthenticationToken)
        assertSame(principal, authentication.principal)
        assertEquals(
            listOf("ROLE_PARENT"),
            authentication.authorities.map { it.authority },
        )
        assertSame(request, chain.request)
    }

    @Test
    fun `revoked token does not establish authentication`() {
        val token = "revoked-token"
        val principal = createPrincipal()

        Mockito.`when`(jwtService.parse(token)).thenReturn(principal)
        Mockito.`when`(
            tokenRevocationService.isRevoked(principal.tokenId)
        ).thenReturn(true)

        filter.doFilter(
            createRequest(token),
            MockHttpServletResponse(),
            MockFilterChain(),
        )

        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `invalid token does not establish authentication`() {
        val token = "invalid-token"

        Mockito.`when`(jwtService.parse(token)).thenReturn(null)

        filter.doFilter(
            createRequest(token),
            MockHttpServletResponse(),
            MockFilterChain(),
        )

        assertNull(SecurityContextHolder.getContext().authentication)
        Mockito.verifyNoInteractions(tokenRevocationService)
    }

    @Test
    fun `request without authorization header does not establish authentication`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        Mockito.verifyNoInteractions(jwtService)
        Mockito.verifyNoInteractions(tokenRevocationService)
        assertSame(request, chain.request)
    }

    @Test
    fun `request with non bearer authorization does not establish authentication`() {
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Basic credentials")
        }

        filter.doFilter(
            request,
            MockHttpServletResponse(),
            MockFilterChain(),
        )

        assertNull(SecurityContextHolder.getContext().authentication)
        Mockito.verifyNoInteractions(jwtService)
        Mockito.verifyNoInteractions(tokenRevocationService)
    }

    @Test
    fun `child token establishes child role authority`() {
        val token = "child-token"
        val principal = createPrincipal(role = Role.CHILD)

        Mockito.`when`(jwtService.parse(token)).thenReturn(principal)
        Mockito.`when`(
            tokenRevocationService.isRevoked(principal.tokenId)
        ).thenReturn(false)

        filter.doFilter(
            createRequest(token),
            MockHttpServletResponse(),
            MockFilterChain(),
        )

        val authentication =
            SecurityContextHolder.getContext().authentication

        assertEquals(
            listOf("ROLE_CHILD"),
            authentication?.authorities?.map { it.authority },
        )
    }

    private fun createRequest(token: String) =
        MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer $token")
        }

    private fun createPrincipal(
        role: Role = Role.PARENT,
    ) = TokenPrincipal(
        userId = UUID.randomUUID(),
        role = role,
        tokenId = UUID.randomUUID(),
        expiresAt = Instant.now().plusSeconds(3600),
    )
}