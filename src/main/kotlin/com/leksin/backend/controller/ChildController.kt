package com.leksin.backend.controller

import com.leksin.backend.dto.ChildCreateRequest
import com.leksin.backend.dto.ChildResponse
import com.leksin.backend.service.ChildService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/parents/{parentId}/children")
class ChildController(
    private val childService: ChildService,
) {

    @PostMapping
    fun create(
        @PathVariable("parentId") parentId: UUID,
        @Valid @RequestBody request: ChildCreateRequest,
    ): ResponseEntity<ChildResponse> {
        val createdChild = childService.create(
            parentId, request
        )

        return ResponseEntity(createdChild, HttpStatus.CREATED)
    }

    @GetMapping
    fun findAll(
        @PathVariable("parentId") parentId: UUID,
    ): ResponseEntity<List<ChildResponse>> {
        return ResponseEntity(
            childService.findAll(parentId),
            HttpStatus.OK,
        )
    }

    @DeleteMapping("/{childId}")
    fun delete(
        @PathVariable("childId") childId: UUID,
        @PathVariable("parentId") parentId: UUID,
    ): ResponseEntity<Unit> {
        return ResponseEntity(
            childService.delete(parentId, childId),
            HttpStatus.NO_CONTENT
        )
    }
}
