package com.leksin.backend.otp

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class OtpEncoder(
    private val passwordEncoder: PasswordEncoder,
) {
    fun encode(code: String): String {
        return passwordEncoder.encode(code)!!
    }

    fun matches(code: String, encodedCode: String): Boolean {
        return passwordEncoder.matches(code, encodedCode)
    }
}