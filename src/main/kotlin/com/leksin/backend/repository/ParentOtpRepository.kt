package com.leksin.backend.repository

import com.leksin.backend.entity.ParentOtp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface ParentOtpRepository : JpaRepository<ParentOtp, UUID> {

    @Query(
        """
        select otp from ParentOtp otp
        where otp.parent.id = :parentId
          and otp.usedAt is null
          and otp.expiresAt > :now
        order by otp.createdAt desc
        limit 1
        """
    )
    fun findLatestValidByParentId(
        parentId: UUID,
        now: Instant,
    ): ParentOtp?

    fun countByParentIdAndCreatedAtAfter(
        parentId: UUID,
        since: Instant,
    ): Long
}