package com.leksin.backend.otp

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OtpEmailListener(
    private val otpSupport: OtpSupport,
) {

    @Async
    @TransactionalEventListener
    fun onOtpEmailRequested(event: OtpEmailRequested) {
        otpSupport.sendOtpEmail(event.recipient, event.subject, event.text)
    }
}
