package com.leksin.backend.service

import com.leksin.backend.dto.request.ChildOtpRequest
import com.leksin.backend.dto.request.ChildOtpVerifyRequest
import com.leksin.backend.dto.response.AuthResponse
import com.leksin.backend.entity.ChildOtp
import com.leksin.backend.entity.Children
import com.leksin.backend.entity.Role
import com.leksin.backend.exception.InvalidOtpException
import com.leksin.backend.otp.OtpEncoder
import com.leksin.backend.otp.OtpGenerator
import com.leksin.backend.otp.OtpPolicy
import com.leksin.backend.otp.OtpSupport
import com.leksin.backend.repository.ChildOtpRepository
import com.leksin.backend.repository.ChildRepository
import com.leksin.backend.security.JwtService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
@Transactional(readOnly = true)
class ChildOtpService(
    private val childRepository: ChildRepository,
    private val childOtpRepository: ChildOtpRepository,
    private val otpGenerator: OtpGenerator,
    private val otpEncoder: OtpEncoder,
    private val otpPolicy: OtpPolicy,
    private val otpSupport: OtpSupport,
    private val clock: Clock,
    private val jwtService: JwtService,
) {

    @Transactional
    fun requestOtp(request: ChildOtpRequest) {
        val child = findAndLockChild(
            request.parentUsername,
            request.nickname,
        ) ?: return

        val childId = child.id!!
        val now = clock.instant()

        otpSupport.hasExceededRequestLimit(now) { windowStart ->
            childOtpRepository.countByChildIdAndCreatedAtAfter(
                childId,
                windowStart,
            )
        }

        childOtpRepository.findLatestValidByChildId(
            childId,
            now,
        )?.usedAt = now

        val code = otpGenerator.generate()

        val otp = ChildOtp(
            child = child,
            codeHash = otpEncoder.encode(code),
            expiresAt = now.plus(otpPolicy.expiration),
        )

        childOtpRepository.save(otp)

        otpSupport.sendOtpEmail(
            recipient = child.parent.email,
            subject = "LEKSIN Child Login OTP",
            text = """
                Your login code is $code.
                It expires in ${otpSupport.expirationMinutes} minutes.
            """.trimIndent(),
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

        if (!otpEncoder.matches(request.code, otp.codeHash)) {
            otpSupport.registerFailedAttempt(otp, now)
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
}
