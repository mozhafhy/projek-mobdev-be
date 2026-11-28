package com.leksin.backend.otp

/** Deliberately not a data class: a generated toString() would expose the code in [text]. */
class OtpEmailRequested(
    val recipient: String,
    val subject: String,
    val text: String,
)
