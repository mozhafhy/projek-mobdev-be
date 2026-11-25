package com.leksin.backend.service

import com.leksin.backend.dto.response.AuthResponse
import com.leksin.backend.dto.request.ParentLoginRequest
import com.leksin.backend.dto.request.ParentProfileUpdateRequest
import com.leksin.backend.dto.request.ParentRegisterRequest
import com.leksin.backend.dto.response.ParentResponse
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
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ParentService(
    private val parentRepository: ParentRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    @Transactional
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

    @Transactional
    fun updateProfile(
        parentId: UUID,
        request: ParentProfileUpdateRequest,
    ): ParentResponse {
        val parent = parentRepository.findById(parentId)
            .orElseThrow { ParentNotFoundException() }

        request.username?.let { changeUsername(parent, parentId, it) }
        request.email?.let { parent.email = it }

        return parent.toParentResponse()
    }

    private fun changeUsername(parent: Parent, parentId: UUID, username: String) {
        if (username == parent.username) return
        if (parentRepository.existsByUsernameAndIdNot(username, parentId)) {
            throw DuplicateUsernameException()
        }
        parent.username = username
    }
}
