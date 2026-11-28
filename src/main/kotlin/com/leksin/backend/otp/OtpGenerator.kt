package com.leksin.backend.otp

import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class OtpGenerator {
    private val secureRandom = SecureRandom()

    fun generate(): String {
        return "%06d".format(secureRandom.nextInt(1_000_000))
    }
}