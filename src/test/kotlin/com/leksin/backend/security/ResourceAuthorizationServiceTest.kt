package com.leksin.backend.security

import com.leksin.backend.entity.Children
import com.leksin.backend.entity.Parent
import com.leksin.backend.entity.Role
import com.leksin.backend.repository.ChildRepository
import com.leksin.backend.repository.ParentRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.test.assertFailsWith

class ResourceAuthorizationServiceTest {
    private val parentRepository = Mockito.mock(ParentRepository::class.java)
    private val childRepository = Mockito.mock(ChildRepository::class.java)
    private val service = ResourceAuthorizationService(parentRepository, childRepository)

    private val parentId = UUID.randomUUID()
    private val childId = UUID.randomUUID()
    private val otherUserId = UUID.randomUUID()
    private val parent = Parent(
        id = parentId,
        username = "parent",
        password = "encoded",
        email = "parent@leksin.test",
    )
    private val child = Children(
        id = childId,
        nickname = "child",
        parent = parent,
    )

    @Test
    fun parentCanAccessOwnChild() {
        Mockito.`when`(childRepository.findById(childId)).thenReturn(Optional.of(child))

        service.requireChildAccess(childId, authentication(parentId, Role.PARENT))
    }

    @Test
    fun childCanAccessOwnResource() {
        Mockito.`when`(childRepository.findById(childId)).thenReturn(Optional.of(child))

        service.requireChildAccess(childId, authentication(childId, Role.CHILD))
    }

    @Test
    fun unrelatedParentCannotAccessChild() {
        Mockito.`when`(childRepository.findById(childId)).thenReturn(Optional.of(child))

        assertFailsWith<AccessDeniedException> {
            service.requireChildAccess(childId, authentication(otherUserId, Role.PARENT))
        }
    }

    @Test
    fun childCannotAccessAnotherChildResource() {
        Mockito.`when`(childRepository.findById(childId)).thenReturn(Optional.of(child))

        assertFailsWith<AccessDeniedException> {
            service.requireChildAccess(childId, authentication(otherUserId, Role.CHILD))
        }
    }

    @Test
    fun parentCannotUseAnotherParentsProfile() {
        Mockito.`when`(parentRepository.findById(parentId)).thenReturn(Optional.of(parent))

        assertFailsWith<AccessDeniedException> {
            service.requireOwnParentProfile("someone-else", authentication(parentId, Role.PARENT))
        }
    }

    @Test
    fun childRoleCannotAccessParentProfile() {
        assertFailsWith<AccessDeniedException> {
            service.requireOwnParentProfile("parent", authentication(childId, Role.CHILD))
        }

        Mockito.verifyNoInteractions(parentRepository)
    }

    private fun authentication(userId: UUID, role: Role) =
        UsernamePasswordAuthenticationToken(
            TokenPrincipal(
                userId = userId,
                role = role,
                tokenId = UUID.randomUUID(),
                expiresAt = Instant.now().plusSeconds(3600),
            ),
            null,
            emptyList(),
        )
}
