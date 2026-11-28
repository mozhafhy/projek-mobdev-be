package com.leksin.backend.otp

import com.leksin.backend.dto.request.ChildCreateRequest
import com.leksin.backend.dto.request.ChildOtpRequest
import com.leksin.backend.dto.request.ParentProfileUpdateRequest
import com.leksin.backend.dto.request.ParentRegisterRequest
import com.leksin.backend.repository.ParentOtpRepository
import com.leksin.backend.service.ChildOtpService
import com.leksin.backend.service.ChildService
import com.leksin.backend.service.ParentOtpService
import com.leksin.backend.service.ParentService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.after
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.timeout
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("test")
class OtpDeliveryIntegrationTest {

    @Autowired
    private lateinit var parentService: ParentService

    @Autowired
    private lateinit var parentOtpService: ParentOtpService

    @Autowired
    private lateinit var childService: ChildService

    @Autowired
    private lateinit var childOtpService: ChildOtpService

    @Autowired
    private lateinit var parentOtpRepository: ParentOtpRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @MockitoBean
    private lateinit var emailSender: JavaMailSender

    @Test
    fun `fourth request sends no email and stores no OTP`() {
        val parent = parentService.register(registration("limit01"))

        repeat(3) { parentOtpService.requestOtp(parent.username) }

        verify(emailSender, after(1_000).times(3)).send(any<SimpleMailMessage>())
        assertEquals(3, countOtps(parent.id))
    }

    @Test
    fun `rolled back transaction sends no email`() {
        TransactionTemplate(transactionManager).executeWithoutResult { status ->
            parentService.register(registration("rollback01"))
            status.setRollbackOnly()
        }

        verify(emailSender, after(1_000).never()).send(any<SimpleMailMessage>())
    }

    @Test
    fun `email change revokes the old OTP even when the request limit is reached`() {
        val parent = parentService.register(registration("change01"))
        repeat(2) { parentOtpService.requestOtp(parent.username) }
        assertNotNull(parentOtpRepository.findLatestValidByParentId(parent.id, Instant.now()))

        parentService.updateProfile(
            parent.id,
            ParentProfileUpdateRequest(email = "changed01@example.com"),
        )

        assertEquals(null, parentOtpRepository.findLatestValidByParentId(parent.id, Instant.now()))
        verify(emailSender, timeout(1_000).times(3)).send(any<SimpleMailMessage>())
    }

    @Test
    fun `child receives no more than three codes in the short window`() {
        val parent = parentService.register(registration("childlimit01"))
        childService.create(parent.id, ChildCreateRequest(nickname = "Bintang"))

        repeat(4) { childOtpService.requestOtp(ChildOtpRequest(parent.username, "Bintang")) }

        val parentVerificationEmails = 1
        val childLoginEmails = 3
        verify(emailSender, after(1_000).times(parentVerificationEmails + childLoginEmails))
            .send(any<SimpleMailMessage>())
    }

    private fun registration(username: String) = ParentRegisterRequest(
        username = username,
        password = "password123",
        email = "$username@example.com",
    )

    private fun countOtps(parentId: UUID): Long =
        parentOtpRepository.countByParentIdAndCreatedAtAfter(parentId, Instant.EPOCH)
}
