package com.leksin.backend.exception

class ChildNotFoundException(
    message: String? = null,
) : DomainException(message ?: "Child not found")
