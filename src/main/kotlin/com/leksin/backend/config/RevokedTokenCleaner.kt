package com.leksin.backend.config

import com.leksin.backend.repository.RevokedTokenRepository
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Configuration
@EnableScheduling
class SchedulingConfig

@Component
class RevokedTokenCleaner(
    private val revokedTokenRepository: RevokedTokenRepository,
) {
    @Scheduled(fixedDelayString = "PT1H")
    @Transactional
    fun purgeExpired() {
        revokedTokenRepository.deleteExpiredBefore(Instant.now())
    }
}