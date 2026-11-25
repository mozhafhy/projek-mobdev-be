package com.leksin.backend.controller

import com.leksin.backend.entity.Role
import com.leksin.backend.security.JwtService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jwtService: JwtService

    @Test
    fun `parent can logout successfully`() {
        val token = createToken(Role.PARENT)

        mockMvc.perform(
            post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isNoContent())
    }

    @Test
    fun `child can logout successfully`() {
        val token = createToken(Role.CHILD)

        mockMvc.perform(
            post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isNoContent())
    }

    @Test
    fun `revoked token cannot access protected endpoint`() {
        val token = createToken(Role.PARENT)

        mockMvc.perform(
            post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isNoContent())

        mockMvc.perform(
            post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isUnauthorized())
    }

    @Test
    fun `logout without token returns unauthorized`() {
        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isUnauthorized())
    }

    @Test
    fun `logout with malformed token returns unauthorized`() {
        mockMvc.perform(
            post("/api/auth/logout")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer invalid-token"
                )
        )
            .andExpect(status().isUnauthorized())
    }

    @Test
    fun `logout with non bearer authorization returns unauthorized`() {
        mockMvc.perform(
            post("/api/auth/logout")
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Basic credentials"
                )
        )
            .andExpect(status().isUnauthorized())
    }

    private fun createToken(role: Role): String =
        jwtService.generateToken(
            userId = java.util.UUID.randomUUID(),
            role = role,
        )
}
