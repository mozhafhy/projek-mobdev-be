package com.leksin.backend.controller

import com.leksin.backend.dto.ParentRegisterRequest
import com.leksin.backend.dto.ParentResponse
import com.leksin.backend.service.ParentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/parents")
class ParentController(
    private val parentService: ParentService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody request: ParentRegisterRequest,
    ): ResponseEntity<ParentResponse> {
        val result = parentService.register(request)
        return ResponseEntity.created(URI("/${result.id}")).body(result)
    }

    @GetMapping("/{username}")
    fun findByUsername(
        @PathVariable username: String,
    ): ResponseEntity<ParentResponse> {
        val result = parentService.findByUsername(username)
        return ResponseEntity.ok(result)
    }
}
