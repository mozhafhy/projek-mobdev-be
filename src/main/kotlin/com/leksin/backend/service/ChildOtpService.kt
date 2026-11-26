package com.leksin.backend.service

import com.leksin.backend.dto.response.AuthResponse
import com.leksin.backend.dto.request.ChildOtpRequest
import com.leksin.backend.dto.request.ChildOtpVerifyRequest
import com.leksin.backend.entity.ChildOtp
import com.leksin.backend.entity.Children
import com.leksin.backend.entity.Role
import com.leksin.backend.exception.InvalidOtpException
import com.leksin.backend.repository.ChildOtpRepository
import com.leksin.backend.repository.ChildRepository
import com.leksin.backend.security.JwtService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ChildOtpService(
    private val childRepository: ChildRepository,
    private val childOtpRepository: ChildOtpRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailSender: JavaMailSender,
    @Value($$"${app.mail.from}")
    private val senderAddress: String,
    private val clock: Clock,
    private val jwtService: JwtService,
) {

    private val secureRandom = SecureRandom()
    private val logger = LoggerFactory.getLogger(ChildOtpService::class.java)

    private companion object {
        const val MAX_FAILED_ATTEMPTS = 5
        const val OTP_EXPIRATION_PERIOD = 1L
        const val MAX_OTP_REQUESTS = 3
        val OTP_REQUEST_WINDOW: Duration = Duration.ofMinutes(5)
    }

    @Transactional
    fun requestOtp(request: ChildOtpRequest) {
        val child = findAndLockChild(
            request.parentUsername,
            request.nickname,
        ) ?: return

        val childId = child.id!!
        val now = clock.instant()

        if (hasExceededRequestLimit(childId, now)) return

        childOtpRepository.findLatestValidByChildId(
            childId,
            now,
        )?.usedAt = now

        val code = "%04d".format(secureRandom.nextInt(10_000))

        val otp = ChildOtp(
            child = child,
            codeHash = passwordEncoder.encode(code)!!,
            expiresAt = now.plus(Duration.ofMinutes(OTP_EXPIRATION_PERIOD)),
        )

        childOtpRepository.save(otp)

        sendOtpEmail(
            recipient = child.parent.email,
            code = code,
        )
    }

    @Transactional(noRollbackFor = [InvalidOtpException::class])
    fun verifyOtp(request: ChildOtpVerifyRequest): AuthResponse {
        val child = findAndLockChild(
            request.parentUsername,
            request.nickname,
        ) ?: throw InvalidOtpException()

        val childId = child.id!!
        val now = clock.instant()

        val otp = childOtpRepository.findLatestValidByChildId(childId, now) ?: throw InvalidOtpException()

        if (!passwordEncoder.matches(request.code, otp.codeHash)) {
            registerFailedAttempt(otp, now)
            throw InvalidOtpException()
        }

        otp.usedAt = now
        return AuthResponse(
            userId = childId,
            username = child.nickname,
            role = Role.CHILD.name,
            accessToken = jwtService.generateToken(
                childId,
                Role.CHILD,
            ),
            expiresIn = jwtService.expirationSeconds,
        )
    }

    private fun findAndLockChild(parentUsername: String, nickname: String): Children? =
        childRepository.findByNicknameAndParentUsername(nickname, parentUsername)
            ?.also { childRepository.lockById(it.id!!) }

    private fun sendOtpEmail(recipient: String, code: String) {
        try {
            emailSender.send(buildOtpEmail(recipient, code))
        } catch (e: MailException) {
            logger.error("Gagal mengirim email OTP: ${e.message} | sender: $senderAddress")
            throw e
        }
    }

    private fun buildOtpEmail(recipient: String, code: String) =
        SimpleMailMessage().apply {
            from = senderAddress
            setTo(recipient)
            subject = "LEKSIN Child Login OTP"
            text = "Your login code is $code.\nIt expires in $OTP_EXPIRATION_PERIOD minutes."
        }

    private fun registerFailedAttempt(otp: ChildOtp, now: Instant) {
        otp.failedAttempts++
        if (otp.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            otp.usedAt = now
        }
    }

    private fun hasExceededRequestLimit(childId: UUID, now: Instant): Boolean =
        childOtpRepository.countByChildIdAndCreatedAtAfter(
            childId,
            now.minus(OTP_REQUEST_WINDOW),
        ) >= MAX_OTP_REQUESTS
}
