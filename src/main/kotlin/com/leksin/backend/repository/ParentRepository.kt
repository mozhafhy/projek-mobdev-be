package com.leksin.backend.repository

import com.leksin.backend.entity.Parent
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ParentRepository : JpaRepository<Parent, UUID> {

    fun findByUsername(username: String): Parent?

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun existsByUsernameAndIdNot(
        username: String,
        id: UUID,
    ): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "select parent from Parent parent where parent.username = :username"
    )
    fun findByUsernameForUpdate(
        @Param("username") username: String,
    ): Parent?
}