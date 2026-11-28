package com.leksin.backend.security

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IpRateLimiterTest {

    private val start = Instant.parse("2026-11-28T04:00:00Z")
    private val clock = mock<Clock>()
    private val limiter = IpRateLimiter(clock)

    @Test
    fun `rejects requests above the limit until the window expires`() {
        whenever(clock.instant()).thenReturn(start)
        repeat(IpRateLimiter.MAX_REQUESTS) { assertTrue(limiter.tryAcquire("client")) }

        assertFalse(limiter.tryAcquire("client"))
        assertTrue(limiter.tryAcquire("other-client"))

        whenever(clock.instant()).thenReturn(start.plus(IpRateLimiter.WINDOW))
        assertTrue(limiter.tryAcquire("client"))
    }
}
