package com.leksin.backend.service

import com.leksin.backend.dto.ChildLoginResponse
import com.leksin.backend.dto.ChildOtpRequest
import com.leksin.backend.dto.ChildOtpVerifyRequest
import com.leksin.backend.entity.ChildOtp
import com.leksin.backend.entity.Children
import com.leksin.backend.exception.InvalidOtpException
import com.leksin.backend.repository.ChildOtpRepository
import com.leksin.backend.repository.ChildRepository
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Service
@Transactional(readOnly = true)
class ChildOtpService(
    private val childRepository: ChildRepository,
    private val childOtpRepository: ChildOtpRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailSender: JavaMailSender,
    private val clock: Clock,
) {

    private val secureRandom = SecureRandom()

    private companion object {
        const val MAX_FAILED_ATTEMPTS = 3
        const val OTP_EXPIRATION_PERIOD = 1L
    }

    @Transactional
    fun requestOtp(request: ChildOtpRequest) {
        val child = findChild(
            request.parentUsername,
            request.nickname,
        ) ?: return

        val childId = child.id!!
        val now = clock.instant()

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

        val message = SimpleMailMessage().apply {
            setTo(child.parent.email)
            subject = "LEKSIN Child Login OTP"
            text = "Your login code is $code. It expires in $OTP_EXPIRATION_PERIOD minutes."
        }

        emailSender.send(message)
    }

    @Transactional(noRollbackFor = [InvalidOtpException::class])
    fun verifyOtp(request: ChildOtpVerifyRequest): ChildLoginResponse {
        val child = findChild(
            request.parentUsername,
            request.nickname,
        ) ?: throw InvalidOtpException()

        val childId = child.id!!
        val now = clock.instant()

        val otp = childOtpRepository.findLatestValidByChildId(childId, now)
            ?: throw InvalidOtpException()

        if (!passwordEncoder.matches(request.code, otp.codeHash)) {
            registerFailedAttempt(otp, now)
            throw InvalidOtpException()
        }

        otp.usedAt = now
        return ChildLoginResponse(childId = childId, nickname = child.nickname)
    }

    private fun findChild(parentUsername: String, nickname: String): Children? =
        childRepository.findByNicknameAndParentUsername(
            nickname,
            parentUsername,
        )

    private fun registerFailedAttempt(otp: ChildOtp, now: Instant) {
        otp.failedAttempts++
        if (otp.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            otp.usedAt = now
        }
    }
}
