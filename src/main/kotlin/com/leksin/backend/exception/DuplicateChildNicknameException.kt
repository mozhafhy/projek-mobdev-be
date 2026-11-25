package com.leksin.backend.exception

class DuplicateChildNicknameException: DomainException(
    "Nickname already exists for this child",
)