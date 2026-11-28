package com.leksin.backend.entity

import com.leksin.backend.otp.OtpAttempt
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "parent_otps")
class ParentOtp(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    var parent: Parent,

    @Column(nullable = false, length = 255)
    var codeHash: String,

    @Column(nullable = false)
    var expiresAt: Instant,

    @Column(nullable = false)
    var createdAt: Instant,

) : OtpAttempt {

    override var failedAttempts: Int = 0
    override var usedAt: Instant? = null
}