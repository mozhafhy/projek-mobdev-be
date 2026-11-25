package com.leksin.backend.service

import com.leksin.backend.dto.AuthResponse
import com.leksin.backend.dto.ParentLoginRequest
import com.leksin.backend.dto.ParentRegisterRequest
import com.leksin.backend.dto.ParentResponse
import com.leksin.backend.entity.Parent
import com.leksin.backend.entity.Role
import com.leksin.backend.entity.toParentResponse
import com.leksin.backend.exception.DuplicateUsernameException
import com.leksin.backend.exception.InvalidCredentialException
import com.leksin.backend.exception.ParentNotFoundException
import com.leksin.backend.repository.ParentRepository
import com.leksin.backend.security.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class ParentService(
    private val parentRepository: ParentRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    fun register(request: ParentRegisterRequest): ParentResponse {
        if (parentRepository.existsByUsername(request.username)) {
            throw DuplicateUsernameException()
        }

        val parent = Parent(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            email = request.email,
        )
        val savedParent = parentRepository.save(parent)

        return savedParent.toParentResponse()
    }

    fun login(request: ParentLoginRequest): AuthResponse {
        val parent = parentRepository.findByUsername(request.username)
            ?: throw InvalidCredentialException(
                "Invalid username or password"
            )

        if (!passwordEncoder.matches(request.password, parent.password)) {
            throw InvalidCredentialException(
                "Invalid username or password"
            )
        }

        val parentId = parent.id!!

        return AuthResponse(
            userId = parentId,
            username = parent.username,
            role = Role.PARENT.name,
            accessToken = jwtService.generateToken(
                parentId,
                Role.PARENT,
            ),
            expiresIn = jwtService.expirationSeconds,
        )
    }

    fun findByUsername(username: String): ParentResponse {
        val parent = parentRepository.findByUsername(username)
            ?: throw ParentNotFoundException(username)

        return parent.toParentResponse()
    }
}
