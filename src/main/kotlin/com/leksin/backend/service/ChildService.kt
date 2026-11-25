package com.leksin.backend.service

import com.leksin.backend.dto.ChildCreateRequest
import com.leksin.backend.dto.ChildResponse
import com.leksin.backend.entity.Children
import com.leksin.backend.entity.toChildResponse
import com.leksin.backend.exception.ChildNotFoundException
import com.leksin.backend.exception.DuplicateChildNicknameException
import com.leksin.backend.exception.ParentNotFoundException
import com.leksin.backend.repository.ChildRepository
import com.leksin.backend.repository.ParentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class ChildService(
    private val childRepository: ChildRepository,
    private val parentRepository: ParentRepository,
) {

    @Transactional
    fun create(
        parentId: UUID,
        request: ChildCreateRequest,
    ): ChildResponse {
        val parent = parentRepository.findById(parentId).orElseThrow { ParentNotFoundException() }

        if (childRepository.existsByNicknameAndParentUsername(
            nickname = request.nickname,
            parentUsername = parent.username,
        )) throw DuplicateChildNicknameException()

        val child = Children(
            nickname = request.nickname,
            parent = parent,
        )

        return childRepository.save(child).toChildResponse()
    }

    fun findAll(parentId: UUID): List<ChildResponse> {
        return childRepository.findAllByParentId(parentId).map {
            it.toChildResponse()
        }
    }

    @Transactional
    fun delete(parentId: UUID, childId: UUID) {
        val child = childRepository.findByIdAndParentId(childId, parentId)
            ?: throw ChildNotFoundException()
        childRepository.delete(child)
    }
}
