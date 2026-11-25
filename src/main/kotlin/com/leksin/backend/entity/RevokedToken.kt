package com.leksin.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "revoked_tokens",
    indexes = [Index(name = "idx_revoked_tokens_expires_at", columnList = "expires_at")],
)
class RevokedToken(
    @Id
    var tokenId: UUID,

    @Column(nullable = false)
    var expiresAt: Instant,

    @Column(nullable = false)
    var revokedAt: Instant = Instant.now(),
)
