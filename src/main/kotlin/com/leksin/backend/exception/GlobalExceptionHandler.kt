package com.leksin.backend.exception

import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(e: DomainException): ProblemDetail {
        val status = statusOf(e)
        return problem(
            status,
            status.reasonPhrase,
            e.message ?: "Domain exception"
        )
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val fieldErrors = ex.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Invalid value for field ${it.field}")
        }
        val body = problem(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            "One or more fields were invalid"
        )
        body.setProperty("errors", fieldErrors)

        return ResponseEntity.badRequest().body(body)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ProblemDetail {
        log.warn("Access denied: {}", e.javaClass.simpleName)
        return problem(
            HttpStatus.FORBIDDEN,
            "Forbidden",
            "You do not have permission to access this resource",
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ProblemDetail =
        problem(
            HttpStatus.BAD_REQUEST,
            "Invalid Request",
            e.message ?: "Invalid request"
        )

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(e: Exception): ProblemDetail {
        log.error("Unexpected exception", e)
        return problem(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Unexpected exception",
            "An unexpected internal error occurred",
        )
    }

    private fun statusOf(e: DomainException): HttpStatus = when (e) {
        is DuplicateUsernameException      -> HttpStatus.CONFLICT
        is DuplicateEmailException         -> HttpStatus.CONFLICT
        is DuplicateChildNicknameException -> HttpStatus.CONFLICT
        is ParentNotFoundException         -> HttpStatus.NOT_FOUND
        is ChildNotFoundException          -> HttpStatus.NOT_FOUND
        is InvalidCredentialException      -> HttpStatus.UNAUTHORIZED
        is InvalidOtpException             -> HttpStatus.UNAUTHORIZED
    }

    private fun problem(
        status: HttpStatus,
        title: String,
        detail: String,
    ): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(status, detail).also {
            it.title = title
            it.setProperty("timestamp", Instant.now())
        }
    }
}
