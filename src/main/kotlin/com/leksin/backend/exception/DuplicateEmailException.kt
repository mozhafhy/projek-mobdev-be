package com.leksin.backend.exception

class DuplicateEmailException : DomainException(
    "Email is already registered"
)