package com.leksin.backend.otp

import java.time.Instant

interface OtpAttempt {
    var failedAttempts: Int
    var usedAt: Instant?
}
