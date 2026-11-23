package com.leksin.backend.exception

class ParentNotFoundException(
    username: String,
) : DomainException("Parent with username: $username not found")
