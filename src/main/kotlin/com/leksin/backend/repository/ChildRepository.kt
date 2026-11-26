package com.leksin.backend.repository

import com.leksin.backend.entity.Children
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ChildRepository : JpaRepository<Children, UUID> {
    fun findAllByParentId(parentId: UUID): List<Children>

    fun findByIdAndParentId(
        childId: UUID,
        parentId: UUID,
    ): Children?

    fun findByNicknameAndParentUsername(
        nickname: String,
        parentUsername: String,
    ): Children?

    fun existsByNicknameAndParentUsername(
        nickname: String,
        parentUsername: String,
    ): Boolean

    fun existsByNicknameAndParentIdAndIdNot(
        nickname: String,
        parentId: UUID,
        childId: UUID,
    ): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Children c where c.id = :childId")
    fun lockById(@Param("childId") childId: UUID): Children?
}
