package com.leksin.backend.service

import com.leksin.backend.dto.request.ChildCreateRequest
import com.leksin.backend.dto.request.ChildProfileUpdateRequest
import com.leksin.backend.dto.response.ChildResponse
import com.leksin.backend.entity.Children
import com.leksin.backend.entity.toChildResponse
import com.leksin.backend.exception.ChildNotFoundException
import com.leksin.backend.exception.DuplicateChildNicknameException
import com.leksin.backend.exception.ParentNotFoundException
import com.leksin.backend.repository.ChildOtpRepository
import com.leksin.backend.repository.ChildRepository
import com.leksin.backend.repository.GameHistoryRepository
import com.leksin.backend.repository.GameModeProgressRepository
import com.leksin.backend.repository.GameProgressRepository
import com.leksin.backend.repository.ParentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class ChildService(
    private val childRepository: ChildRepository,
    private val parentRepository: ParentRepository,
    private val childOtpRepository: ChildOtpRepository,
    private val gameHistoryRepository: GameHistoryRepository,
    private val gameProgressRepository: GameProgressRepository,
    private val gameModeProgressRepository: GameModeProgressRepository,
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
        deleteChildData(childId)
        childRepository.delete(child)
    }

    private fun deleteChildData(childId: UUID) {
        childOtpRepository.deleteAllByChildId(childId)
        gameHistoryRepository.deleteAllByChildId(childId)
        gameModeProgressRepository.deleteAllByChildId(childId)
        gameProgressRepository.deleteAllByChildId(childId)
    }

    @Transactional
    fun updateProfile(
        parentId: UUID,
        childId: UUID,
        request: ChildProfileUpdateRequest,
    ): ChildResponse {
        val child = childRepository.findByIdAndParentId(
            childId = childId,
            parentId = parentId,
        ) ?: throw ChildNotFoundException()

        val nicknameExists =
            childRepository.existsByNicknameAndParentIdAndIdNot(
                nickname = request.nickname,
                parentId = parentId,
                childId = childId,
            )

        if (nicknameExists) {
            throw DuplicateChildNicknameException()
        }

        child.nickname = request.nickname

        return childRepository.save(child).toChildResponse()
    }
}
