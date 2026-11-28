package com.leksin.backend.service

import com.leksin.backend.entity.Parent
import com.leksin.backend.entity.ParentOtp
import com.leksin.backend.otp.OtpEmailRequested
import com.leksin.backend.otp.OtpEncoder
import com.leksin.backend.otp.OtpGenerator
import com.leksin.backend.otp.OtpPolicy
import com.leksin.backend.otp.OtpRequestLimit
import com.leksin.backend.otp.OtpSupport
import com.leksin.backend.repository.ParentOtpRepository
import com.leksin.backend.repository.ParentRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.assertNull

class ParentOtpServiceTest {

    private val now = Instant.parse("2026-11-28T04:00:00Z")
    private val parentId = UUID.randomUUID()
    private val parent = Parent(
        id = parentId,
        username = "parent01",
        password = "hash",
        email = "parent@example.com",
        emailVerified = false,
    )
    private val activeOtp = ParentOtp(
        parent = parent,
        codeHash = "old-hash",
        expiresAt = now.plusSeconds(60),
        createdAt = now.minusSeconds(60),
    )

    private val parentOtpRepository = mock<ParentOtpRepository>()
    private val eventPublisher = mock<ApplicationEventPublisher>()
    private val otpPolicy = OtpPolicy()

    private val service = ParentOtpService(
        parentRepository = mock<ParentRepository>(),
        parentOtpRepository = parentOtpRepository,
        otpGenerator = mock<OtpGenerator> { on { generate() } doReturn "123456" },
        otpEncoder = mock<OtpEncoder> { on { encode("123456") } doReturn "new-hash" },
        otpPolicy = otpPolicy,
        otpSupport = OtpSupport(otpPolicy, mock(), "no-reply@leksin.test"),
        clock = Clock.fixed(now, ZoneOffset.UTC),
        eventPublisher = eventPublisher,
    )

    @Test
    fun `fourth request in the short window changes nothing`() {
        givenRequestCount(limit = otpPolicy.requestLimits[0], count = 3)

        service.sendVerificationOtp(parent)

        assertRequestIgnored()
    }

    @Test
    fun `request above the daily limit changes nothing`() {
        givenRequestCount(limit = otpPolicy.requestLimits[1], count = 10)

        service.sendVerificationOtp(parent)

        assertRequestIgnored()
    }

    private fun givenRequestCount(limit: OtpRequestLimit, count: Long) {
        whenever(
            parentOtpRepository.countByParentIdAndCreatedAtAfter(parentId, now.minus(limit.window))
        ).thenReturn(count)
        whenever(parentOtpRepository.findLatestValidByParentId(parentId, now)).thenReturn(activeOtp)
    }

    private fun assertRequestIgnored() {
        assertNull(activeOtp.usedAt)
        verify(parentOtpRepository, never()).save(any<ParentOtp>())
        verify(eventPublisher, never()).publishEvent(any<OtpEmailRequested>())
    }
}
