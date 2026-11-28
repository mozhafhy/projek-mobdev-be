package com.leksin.backend.service

import com.leksin.backend.dto.request.ChildOtpRequest
import com.leksin.backend.entity.ChildOtp
import com.leksin.backend.entity.Children
import com.leksin.backend.entity.Parent
import com.leksin.backend.otp.OtpEmailRequested
import com.leksin.backend.otp.OtpEncoder
import com.leksin.backend.otp.OtpGenerator
import com.leksin.backend.otp.OtpPolicy
import com.leksin.backend.otp.OtpRequestLimit
import com.leksin.backend.otp.OtpSupport
import com.leksin.backend.repository.ChildOtpRepository
import com.leksin.backend.repository.ChildRepository
import com.leksin.backend.security.JwtService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
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
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChildOtpServiceTest {

    private val now = Instant.parse("2026-11-28T04:00:00Z")
    private val childId = UUID.randomUUID()
    private val child = Children(
        id = childId,
        nickname = "Bintang",
        parent = Parent(username = "parent01", password = "hash", email = "parent@example.com"),
    )
    private val activeOtp = ChildOtp(
        child = child,
        codeHash = "old-hash",
        expiresAt = now.plusSeconds(60),
        createdAt = now.minusSeconds(60),
    )
    private val request = ChildOtpRequest(parentUsername = "parent01", nickname = "Bintang")

    private val childRepository = mock<ChildRepository> {
        on { findByNicknameAndParentUsernameForUpdate("Bintang", "parent01") } doReturn child
    }
    private val childOtpRepository = mock<ChildOtpRepository>()
    private val eventPublisher = mock<ApplicationEventPublisher>()
    private val otpPolicy = OtpPolicy()

    private val service = ChildOtpService(
        childRepository = childRepository,
        childOtpRepository = childOtpRepository,
        otpGenerator = mock<OtpGenerator> { on { generate() } doReturn "123456" },
        otpEncoder = mock<OtpEncoder> { on { encode("123456") } doReturn "new-hash" },
        otpPolicy = otpPolicy,
        otpSupport = OtpSupport(otpPolicy, mock(), "no-reply@leksin.test"),
        clock = Clock.fixed(now, ZoneOffset.UTC),
        eventPublisher = eventPublisher,
        jwtService = mock<JwtService>(),
    )

    @Test
    fun `allowed request replaces the active OTP using the injected clock`() {
        givenActiveOtp()

        service.requestOtp(request)

        val savedOtp = argumentCaptor<ChildOtp>()
        verify(childOtpRepository).save(savedOtp.capture())
        assertEquals(now, savedOtp.firstValue.createdAt)
        assertEquals(now, activeOtp.usedAt)
        verify(eventPublisher).publishEvent(any<OtpEmailRequested>())
    }

    @Test
    fun `fourth request in the short window changes nothing`() {
        givenRequestCount(limit = otpPolicy.requestLimits[0], count = 3)

        service.requestOtp(request)

        assertRequestIgnored()
    }

    @Test
    fun `request above the daily limit changes nothing`() {
        givenRequestCount(limit = otpPolicy.requestLimits[1], count = 10)

        service.requestOtp(request)

        assertRequestIgnored()
    }

    private fun givenActiveOtp() {
        whenever(childOtpRepository.findLatestValidByChildId(childId, now)).thenReturn(activeOtp)
    }

    private fun givenRequestCount(limit: OtpRequestLimit, count: Long) {
        givenActiveOtp()
        whenever(
            childOtpRepository.countByChildIdAndCreatedAtAfter(childId, now.minus(limit.window))
        ).thenReturn(count)
    }

    private fun assertRequestIgnored() {
        assertNull(activeOtp.usedAt)
        verify(childOtpRepository, never()).save(any<ChildOtp>())
        verify(eventPublisher, never()).publishEvent(any<OtpEmailRequested>())
    }
}
