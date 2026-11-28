package com.leksin.backend.security

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/** In-memory fixed window: valid for a single backend instance and reset on restart. */
@Component
class IpRateLimiter(
    private val clock: Clock,
) {
    private val windows = ConcurrentHashMap<String, RequestWindow>()

    fun tryAcquire(key: String): Boolean {
        val now = clock.instant()
        val window = windows.compute(key) { _, current ->
            if (current == null || current.isExpired(now)) {
                RequestWindow(startedAt = now, requestCount = 1)
            } else {
                current.copy(requestCount = current.requestCount + 1)
            }
        }!!

        return window.requestCount <= MAX_REQUESTS
    }

    @Scheduled(fixedDelayString = "PT10M")
    fun evictExpiredWindows() {
        val now = clock.instant()
        windows.values.removeIf { it.isExpired(now) }
    }

    private data class RequestWindow(
        val startedAt: Instant,
        val requestCount: Int,
    ) {
        fun isExpired(now: Instant): Boolean = !now.isBefore(startedAt.plus(WINDOW))
    }

    companion object {
        const val MAX_REQUESTS = 20
        val WINDOW: Duration = Duration.ofMinutes(10)
    }
}
