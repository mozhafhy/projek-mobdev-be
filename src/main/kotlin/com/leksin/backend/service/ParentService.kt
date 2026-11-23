package com.leksin.backend.service

import com.leksin.backend.dto.ParentRegisterRequest
import com.leksin.backend.dto.ParentResponse
import com.leksin.backend.entity.Parent
import com.leksin.backend.entity.toParentResponse
import com.leksin.backend.exception.DuplicateUsernameException
import com.leksin.backend.exception.ParentNotFoundException
import com.leksin.backend.repository.ParentRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class ParentService(
    private val parentRepository: ParentRepository,
    private val passwordEncoder: PasswordEncoder,
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

    fun findByUsername(username: String): ParentResponse {
        val parent = parentRepository.findByUsername(username)
            ?: throw ParentNotFoundException(username)

        return parent.toParentResponse()
    }
}
