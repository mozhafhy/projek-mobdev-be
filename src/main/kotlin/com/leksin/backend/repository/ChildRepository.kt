package com.leksin.backend.repository

import com.leksin.backend.entity.Children
import org.springframework.data.jpa.repository.JpaRepository
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
}
