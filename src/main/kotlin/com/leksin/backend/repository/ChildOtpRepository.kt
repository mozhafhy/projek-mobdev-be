package com.leksin.backend.repository

import com.leksin.backend.entity.ChildOtp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface ChildOtpRepository: JpaRepository<ChildOtp, UUID> {
    @Query(
        """
    select otp from ChildOtp otp
    where otp.child.id = :childId
      and otp.usedAt is null
      and otp.expiresAt > :now
    order by otp.createdAt desc
    limit 1
    """
    )
    fun findLatestValidByChildId(childId: UUID, now: Instant): ChildOtp?

    fun countByChildIdAndCreatedAtAfter(childId: UUID, since: Instant): Long
}
