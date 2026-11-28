package com.leksin.backend.security

import com.leksin.backend.exception.TooManyRequestsException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class IpRateLimitInterceptor(
    private val rateLimiter: IpRateLimiter,
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val key = "${request.remoteAddr}|${request.requestURI}"

        if (!rateLimiter.tryAcquire(key)) {
            throw TooManyRequestsException()
        }

        return true
    }
}
