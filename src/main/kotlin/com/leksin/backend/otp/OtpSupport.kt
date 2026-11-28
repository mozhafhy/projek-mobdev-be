package com.leksin.backend.otp

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OtpSupport(
    private val otpPolicy: OtpPolicy,
    private val emailSender: JavaMailSender,
    @Value($$"${app.mail.from}") private val senderAddress: String,
) {
    private val logger = LoggerFactory.getLogger(OtpSupport::class.java)

    fun registerFailedAttempt(
        otp: OtpAttempt,
        now: Instant,
    ) {
        otp.failedAttempts++

        if (otp.failedAttempts >= otpPolicy.maxFailedAttempts) {
            otp.usedAt = now
        }
    }

    fun hasExceededRequestLimit(
        now: Instant,
        countRequestsSince: (Instant) -> Long,
    ): Boolean {
        val windowStart = now.minus(otpPolicy.requestWindow)

        return countRequestsSince(windowStart) >= otpPolicy.maxRequests
    }

    fun sendOtpEmail(
        recipient: String,
        subject: String,
        text: String,
    ) {
        try {
            emailSender.send(
                SimpleMailMessage().apply {
                    from = senderAddress
                    setTo(recipient)
                    this.subject = subject
                    this.text = text
                }
            )
        } catch (exception: MailException) {
            logger.error("Failed to send OTP email", exception)
            throw exception
        }
    }

    val expirationMinutes: Long
        get() = otpPolicy.expiration.seconds / 60
}
