
package com.leksin.backend.otp

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.mail.MailSendException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class OtpSupportTest {

    private val emailSender = mock<JavaMailSender>()

    private val otpPolicy = OtpPolicy()

    private val support = OtpSupport(
        otpPolicy = otpPolicy,
        emailSender = emailSender,
        senderAddress = "noreply@leksin.my.id",
    )

    private val now = Instant.parse("2026-11-28T04:00:00Z")

    private class TestOtpAttempt(
        override var failedAttempts: Int = 0,
        override var usedAt: Instant? = null,
    ) : OtpAttempt

    // Failed attempt tests

    @Test
    fun `registerFailedAttempt should increment failed attempts`() {
        val otp = TestOtpAttempt()

        support.registerFailedAttempt(otp, now)

        assertEquals(1, otp.failedAttempts)
        assertNull(otp.usedAt)
    }

    @Test
    fun `registerFailedAttempt should not invalidate OTP below maximum attempts`() {
        val otp = TestOtpAttempt(failedAttempts = 1)

        support.registerFailedAttempt(otp, now)

        assertEquals(2, otp.failedAttempts)
        assertNull(otp.usedAt)
    }

    @Test
    fun `registerFailedAttempt should invalidate OTP at maximum attempts`() {
        val otp = TestOtpAttempt(failedAttempts = 2)

        support.registerFailedAttempt(otp, now)

        assertEquals(3, otp.failedAttempts)
        assertEquals(now, otp.usedAt)
    }

    @Test
    fun `registerFailedAttempt should keep OTP invalidated above maximum attempts`() {
        val otp = TestOtpAttempt(
            failedAttempts = 3,
            usedAt = now.minusSeconds(30),
        )

        support.registerFailedAttempt(otp, now)

        assertEquals(4, otp.failedAttempts)
        assertEquals(now, otp.usedAt)
    }

    // Request limit tests

    @Test
    fun `hasExceededRequestLimit should return false below request limit`() {
        var receivedWindowStart: Instant? = null

        val result = support.hasExceededRequestLimit(now) { windowStart ->
            receivedWindowStart = windowStart
            2L
        }

        assertFalse(result)
        assertEquals(
            now.minus(otpPolicy.requestWindow),
            receivedWindowStart,
        )
    }

    @Test
    fun `hasExceededRequestLimit should return true at request limit`() {
        val result = support.hasExceededRequestLimit(now) {
            3L
        }

        assertTrue(result)
    }

    @Test
    fun `hasExceededRequestLimit should return true above request limit`() {
        val result = support.hasExceededRequestLimit(now) {
            4L
        }

        assertTrue(result)
    }

    @Test
    fun `hasExceededRequestLimit should pass correct window start to callback`() {
        val expectedWindowStart = now.minusSeconds(5 * 60)
        var actualWindowStart: Instant? = null

        support.hasExceededRequestLimit(now) { windowStart ->
            actualWindowStart = windowStart
            0L
        }

        assertEquals(expectedWindowStart, actualWindowStart)
    }

    // Email tests

    @Test
    fun `sendOtpEmail should send correctly configured email`() {
        support.sendOtpEmail(
            recipient = "parent@example.com",
            subject = "LEKSIN Email Verification",
            text = "Your verification code is 123456.",
        )

        val messageCaptor = argumentCaptor<SimpleMailMessage>()

        verify(emailSender).send(messageCaptor.capture())

        val message = messageCaptor.firstValue

        assertEquals("noreply@leksin.my.id", message.from)
        assertTrue(
            message.to!!.contentEquals(
                arrayOf("parent@example.com"),
            ),
        )
        assertEquals("LEKSIN Email Verification", message.subject)
        assertEquals("Your verification code is 123456.", message.text)
    }

    @Test
    fun `sendOtpEmail should propagate MailException`() {
        val exception = MailSendException("SMTP server unavailable")

        doThrow(exception)
            .whenever(emailSender)
            .send(any<SimpleMailMessage>())

        try {
            support.sendOtpEmail(
                recipient = "parent@example.com",
                subject = "LEKSIN Email Verification",
                text = "Your verification code is 123456.",
            )
            throw AssertionError("Expected MailSendException")
        } catch (actual: MailSendException) {
            assertSame(exception, actual)
        }
    }

    // Expiration tests

    @Test
    fun `expirationMinutes should return policy expiration in minutes`() {
        assertEquals(2L, support.expirationMinutes)
    }
}
