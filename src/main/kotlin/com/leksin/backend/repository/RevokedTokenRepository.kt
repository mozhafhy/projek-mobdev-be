package com.leksin.backend.repository

import com.leksin.backend.entity.RevokedToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.*

interface RevokedTokenRepository : JpaRepository<RevokedToken, UUID> {

    @Modifying
    @Query(
        value = """
        insert into revoked_tokens (token_id, expires_at, revoked_at)
        select :tokenId, :expiresAt, :revokedAt
        where not exists (
            select 1
            from revoked_tokens
            where token_id = :tokenId
        )
        """,
        nativeQuery = true,
    )
    fun insertIfAbsent(
        @Param("tokenId") tokenId: UUID,
        @Param("expiresAt") expiresAt: Instant,
        @Param("revokedAt") revokedAt: Instant,
    )

    @Modifying
    @Query("delete from RevokedToken t where t.expiresAt < :threshold")
    fun deleteExpiredBefore(@Param("threshold") threshold: Instant): Int
}
