package com.leksin.backend.otp

import java.time.Duration

data class OtpRequestLimit(
    val maxRequests: Int,
    val window: Duration,
)
