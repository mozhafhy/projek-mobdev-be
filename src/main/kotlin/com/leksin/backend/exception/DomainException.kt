package com.leksin.backend.exception

sealed class DomainException(
    message: String,
) : RuntimeException(message)
