package com.leksin.backend.exception

import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(e: DomainException): ProblemDetail {
        val status = when (e) {
            is DuplicateUsernameException -> HttpStatus.CONFLICT
            is ParentNotFoundException -> HttpStatus.NOT_FOUND
            is InvalidCredentialException -> HttpStatus.UNAUTHORIZED
            is ChildNotFoundException -> HttpStatus.NOT_FOUND
            is InvalidOtpException -> HttpStatus.UNAUTHORIZED
            is DuplicateChildNicknameException -> HttpStatus.CONFLICT
        }

        return ProblemDetail.forStatusAndDetail(
            status, e.message ?: "Domain exception"
        ).apply {
            title = status.reasonPhrase
            setProperty("timestamp", Instant.now())
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ProblemDetail {
        val fieldErrors = e.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Invalid value for field ${it.field}")
        }

        return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "One or more fields were invalid"
        ).apply {
            title = "Validation failed"
            setProperty("timestamp", Instant.now())
            setProperty("errors", fieldErrors)
        }
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
        e: AccessDeniedException,
    ): ProblemDetail {
        logger.error("Access denied exception", e)
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "You do not have permission to access this resource",
        ).apply {
            title = "Forbidden"
            setProperty("timestamp", Instant.now())
        }
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        e: IllegalArgumentException,
    ): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            e.message ?: "Invalid request",
        ).apply {
            title = "Invalid Request"
            setProperty("timestamp", Instant.now())
        }
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(e: Exception): ProblemDetail {
        logger.error("Unexpected exception", e)

        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected internal error occurred"
        ).apply {
            title = "Unexpected exception"
            setProperty("timestamp", Instant.now())
        }
    }
}
