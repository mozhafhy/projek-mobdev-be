package com.leksin.backend.service

import com.leksin.backend.repository.RevokedTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class TokenRevocationService(
    private val revokedTokenRepository: RevokedTokenRepository,
) {
    @Transactional
    fun revoke(tokenId: UUID, expiresAt: Instant) {
        revokedTokenRepository.insertIfAbsent(tokenId, expiresAt, Instant.now())
    }

    @Transactional(readOnly = true)
    fun isRevoked(tokenId: UUID): Boolean =
        revokedTokenRepository.existsById(tokenId)
}
