package com.leksin.backend.otp

import org.springframework.stereotype.Component
import java.time.Duration

@Component
class OtpPolicy {
    val maxFailedAttempts: Int = 3
    val expiration: Duration = Duration.ofMinutes(2)
    val requestLimits: List<OtpRequestLimit> = listOf(
        OtpRequestLimit(maxRequests = 3, window = Duration.ofMinutes(5)),
        OtpRequestLimit(maxRequests = 10, window = Duration.ofDays(1)),
    )
}
