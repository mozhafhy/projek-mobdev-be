package com.leksin.backend.repository

import com.leksin.backend.entity.Parent
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ParentRepository : JpaRepository<Parent, UUID> {
    fun findByUsername(username: String): Parent?

    fun existsByUsername(username: String): Boolean

    fun existsByUsernameAndIdNot(
        username: String,
        id: UUID,
    ): Boolean
}
