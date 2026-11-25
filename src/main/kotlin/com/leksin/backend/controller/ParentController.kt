package com.leksin.backend.controller

import com.leksin.backend.dto.response.AuthResponse
import com.leksin.backend.dto.request.ParentLoginRequest
import com.leksin.backend.dto.request.ParentRegisterRequest
import com.leksin.backend.dto.response.ParentResponse
import com.leksin.backend.security.ResourceAuthorizationService
import com.leksin.backend.service.ParentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/parent")
class ParentController(
    private val parentService: ParentService,
    private val authorizationService: ResourceAuthorizationService,
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody request: ParentRegisterRequest,
    ): ResponseEntity<ParentResponse> {
        val result = parentService.register(request)
        return ResponseEntity.created(URI("/${result.id}")).body(result)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: ParentLoginRequest,
    ): ResponseEntity<AuthResponse> {
        val result = parentService.login(request)

        return ResponseEntity.ok(result)
    }

    @GetMapping("/{username}")
    fun getProfile(
        @PathVariable username: String,
        authentication: Authentication,
    ): ResponseEntity<ParentResponse> {
        authorizationService.requireOwnParentProfile(
            username = username,
            authentication = authentication,
        )

        val result = parentService.findByUsername(username)
        return ResponseEntity.ok(result)
    }
}
