package com.leksin.backend.security

import com.leksin.backend.entity.Role
import com.leksin.backend.exception.ChildNotFoundException
import com.leksin.backend.exception.ParentNotFoundException
import com.leksin.backend.repository.ChildRepository
import com.leksin.backend.repository.ParentRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ResourceAuthorizationService(
    private val parentRepository: ParentRepository,
    private val childRepository: ChildRepository,
) {

    fun requireParentAccess(
        parentId: UUID,
        authentication: Authentication,
    ) {
        val principal = getPrincipal(authentication)

        if (
            principal.role != Role.PARENT ||
            principal.userId != parentId
        ) {
            throw AccessDeniedException("Access denied")
        }
    }

    @Transactional(readOnly = true)
    fun requireOwnParentProfile(
        username: String,
        authentication: Authentication,
    ) {
        val principal = getPrincipal(authentication)

        if (principal.role != Role.PARENT) {
            throw AccessDeniedException("Access denied")
        }

        val parent = parentRepository.findById(principal.userId)
            .orElseThrow { ParentNotFoundException() }

        if (parent.username != username) {
            throw AccessDeniedException("Access denied")
        }
    }

    @Transactional(readOnly = true)
    fun requireChildAccess(
        childId: UUID,
        authentication: Authentication,
    ) {
        val principal = getPrincipal(authentication)

        val child = childRepository.findById(childId)
            .orElseThrow { ChildNotFoundException() }

        val isChildOwner =
            principal.role == Role.CHILD &&
                    principal.userId == childId

        val isParentOwner =
            principal.role == Role.PARENT &&
                    principal.userId == child.parent.id

        if (!isChildOwner && !isParentOwner) {
            throw AccessDeniedException("Access denied")
        }
    }

    private fun getPrincipal(
        authentication: Authentication,
    ): TokenPrincipal {
        return authentication.principal as? TokenPrincipal
            ?: throw AccessDeniedException("Access denied")
    }
}
