package com.leksin.backend.otp

import org.springframework.stereotype.Component
import java.time.Duration

@Component
class OtpPolicy {
    val maxFailedAttempts: Int = 3
    val expiration: Duration = Duration.ofMinutes(2)
    val maxRequests: Int = 3
    val requestWindow: Duration = Duration.ofMinutes(5)
}
