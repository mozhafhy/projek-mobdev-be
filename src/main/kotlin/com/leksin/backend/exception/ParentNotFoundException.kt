package com.leksin.backend.exception

class ParentNotFoundException(
    message: String? = null,
) : DomainException(message ?: "Parent not found")
