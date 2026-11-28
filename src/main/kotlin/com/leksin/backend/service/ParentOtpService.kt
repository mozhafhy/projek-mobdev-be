package com.leksin.backend.service

import com.leksin.backend.dto.request.ParentOtpVerifyRequest
import com.leksin.backend.entity.Parent
import com.leksin.backend.entity.ParentOtp
import com.leksin.backend.exception.InvalidOtpException
import com.leksin.backend.otp.OtpEncoder
import com.leksin.backend.otp.OtpGenerator
import com.leksin.backend.otp.OtpPolicy
import com.leksin.backend.otp.OtpSupport
import com.leksin.backend.repository.ParentOtpRepository
import com.leksin.backend.repository.ParentRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
@Transactional(readOnly = true)
class ParentOtpService(
    private val parentRepository: ParentRepository,
    private val parentOtpRepository: ParentOtpRepository,
    private val otpGenerator: OtpGenerator,
    private val otpEncoder: OtpEncoder,
    private val otpPolicy: OtpPolicy,
    private val otpSupport: OtpSupport,
    @Value($$"${app.mail.from}")
    private val clock: Clock,
) {

    @Transactional
    fun sendVerificationOtp(parent: Parent) {
        val parentId = parent.id ?: return
        val now = clock.instant()

        otpSupport.hasExceededRequestLimit(now) { windowStart ->
            parentOtpRepository.countByParentIdAndCreatedAtAfter(
                parentId,
                windowStart,
            )
        }

        parentOtpRepository.findLatestValidByParentId(
            parentId,
            now,
        )?.usedAt = now

        val code = otpGenerator.generate()

        parentOtpRepository.save(
            ParentOtp(
                parent = parent,
                codeHash = otpEncoder.encode(code),
                expiresAt = now.plus(otpPolicy.expiration),
                createdAt = now,
            )
        )

        otpSupport.sendOtpEmail(
            recipient = parent.email,
            subject = "LEKSIN Email Verification",
            text = """
                Your LEKSIN verification code is $code.
        
                This code expires in ${otpSupport.expirationMinutes} minutes.
                Do not share this code with anyone.
            """.trimIndent(),
        )
    }

    @Transactional
    fun requestOtp(username: String) {
        val parent = parentRepository.findByUsernameForUpdate(username)
            ?: return

        if (parent.emailVerified) {
            return
        }

        sendVerificationOtp(parent)
    }

    @Transactional(noRollbackFor = [InvalidOtpException::class])
    fun verifyOtp(request: ParentOtpVerifyRequest) {
        val parent = parentRepository.findByUsernameForUpdate(request.username)
            ?: throw InvalidOtpException()

        if (parent.emailVerified) {
            throw InvalidOtpException()
        }

        val parentId = parent.id!!
        val now = clock.instant()

        val otp = parentOtpRepository.findLatestValidByParentId(
            parentId,
            now,
        ) ?: throw InvalidOtpException()

        if (!otpEncoder.matches(request.code, otp.codeHash)) {
            otpSupport.registerFailedAttempt(otp, now)
            throw InvalidOtpException()
        }

        otp.usedAt = now
        parent.emailVerified = true
    }
}