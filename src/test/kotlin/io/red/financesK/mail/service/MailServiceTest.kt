package io.red.financesK.mail.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.red.financesK.user.model.AppUser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.ReflectionTestUtils
import java.time.Instant
import java.util.*

@ExtendWith(SpringExtension::class)
class MailServiceTest {

    private lateinit var mailService: MailService
    private val mockMailSender: JavaMailSender = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        mailService = MailService(mockMailSender)
        
        // Set up configuration properties using ReflectionTestUtils
        ReflectionTestUtils.setField(mailService, "supportEmail", "support@financesK.com")
        ReflectionTestUtils.setField(mailService, "baseUrl", "https://financesK.com")
    }

    @Test
    fun `sendMailToken should send email with correct parameters`() {
        // Given
        val testMessage = SimpleMailMessage().apply {
            setTo("user@example.com")
            subject = "Test Subject"
            text = "Test message body"
            from = "support@financesK.com"
        }
        
        val messageSlot = slot<SimpleMailMessage>()
        every { mockMailSender.send(capture(messageSlot)) } returns Unit

        // When
        mailService.sendMailToken(testMessage)

        // Then
        verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }

        val capturedMessage = messageSlot.captured
        assertEquals("Test Subject", capturedMessage.subject)
        assertEquals("Test message body", capturedMessage.text)
        assertEquals("user@example.com", capturedMessage.to?.get(0))
    }

    @Test
    fun `constructResetTokenEmail should return properly formatted email`() {
        // Given
        val token = "test-token-123"
        val user = createTestUser()
        val expectedUrl = "https://financesK.com/user/change-password?token=$token"

        // When
        val result = mailService.constructResetTokenEmail(token, user)

        // Then
        assertEquals("Password Reset", result.subject)
        assertEquals("test@example.com", result.to?.get(0))
        assertEquals("support@financesK.com", result.from)
        assertTrue(result.text!!.contains("Reset your password using the following link"))
        assertTrue(result.text!!.contains(expectedUrl))
    }

    @Test
    fun `constructWelcomeEmail should return properly formatted email`() {
        // Given
        val user = createTestUser()

        // When
        val result = mailService.constructWelcomeEmail(user)

        // Then
        assertEquals("Welcome to FinancesK", result.subject)
        assertEquals("test@example.com", result.to?.get(0))
        assertEquals("support@financesK.com", result.from)
        assertTrue(result.text!!.contains("Welcome to FinancesK! Your account has been successfully created."))
    }

    @Test
    fun `constructGenericEmail should return properly formatted email`() {
        // Given
        val user = createTestUser()
        val subject = "Custom Subject"
        val body = "Custom email body content"

        // When
        val result = mailService.constructGenericEmail(subject, body, user)

        // Then
        assertEquals(subject, result.subject)
        assertEquals("test@example.com", result.to?.get(0))
        assertEquals("support@financesK.com", result.from)
        assertEquals(body, result.text)
    }

    @Test
    fun `constructResetTokenEmail should use configured base URL`() {
        // Given
        val token = "base-url-test-token"
        val user = createTestUser()

        // When
        val result = mailService.constructResetTokenEmail(token, user)

        // Then
        assertEquals("Password Reset", result.subject)
        assertTrue(result.text!!.contains("https://financesK.com/user/change-password?token=$token"))
    }

    @Test
    fun `constructResetTokenEmail should handle special characters in token`() {
        // Given
        val token = "special-token-!@#$%^&*()"
        val user = createTestUser()

        // When
        val result = mailService.constructResetTokenEmail(token, user)

        // Then
        assertEquals("Password Reset", result.subject)
        assertTrue(result.text!!.contains("change-password?token=$token"))
    }

    @Test
    fun `constructResetTokenEmail should work with different base URLs`() {
        // Given
        val token = "different-url-test"
        val user = createTestUser()
        val differentBaseUrl = "https://production.financesK.com"

        // Set different base URL
        ReflectionTestUtils.setField(mailService, "baseUrl", differentBaseUrl)

        // When
        val result = mailService.constructResetTokenEmail(token, user)

        // Then
        assertEquals("Password Reset", result.subject)
        assertTrue(result.text!!.contains("$differentBaseUrl/user/change-password?token=$token"))
    }

    @Test
    fun `sendMailToken should handle exception when sending mail`() {
        // Given
        val testMessage = SimpleMailMessage().apply {
            setTo("user@example.com")
            subject = "Test Subject"
            text = "Test message body"
        }
        every { mockMailSender.send(any<SimpleMailMessage>()) } throws RuntimeException("Mail server error")

        // When & Then
        assertThrows(RuntimeException::class.java) {
            mailService.sendMailToken(testMessage)
        }
    }

    @Test
    fun `constructResetTokenEmail should work with user having different email formats`() {
        // Given
        val token = "email-format-test"
        
        val emailFormats = listOf(
            "simple@example.com",
            "user.name@example.com",
            "user+tag@example.com",
            "user123@subdomain.example.com",
            "very.long.email.address@very.long.domain.name.com"
        )

        // When & Then
        emailFormats.forEach { email ->
            val user = createTestUser().copy(email = email)
            val result = mailService.constructResetTokenEmail(token, user)

            assertEquals("Password Reset", result.subject)
            assertEquals(email, result.to?.get(0))
            assertEquals("support@financesK.com", result.from)
        }
    }

    @Test
    fun `constructResetTokenEmail should include both message and URL in body`() {
        // Given
        val token = "body-content-test"
        val user = createTestUser()
        val expectedUrl = "https://financesK.com/user/change-password?token=$token"

        // When
        val result = mailService.constructResetTokenEmail(token, user)

        // Then
        val bodyParts = result.text!!.split(" \r\n")
        assertEquals(2, bodyParts.size)
        assertTrue(bodyParts[0].contains("Reset your password using the following link"))
        assertEquals(expectedUrl, bodyParts[1])
    }

    @Test
    fun `constructResetTokenEmail should handle null user email`() {
        // Given
        val token = "null-email-test"
        val user = createTestUser().copy(email = null)

        // When
        val result = mailService.constructResetTokenEmail(token, user)

        // Then
        assertEquals("Password Reset", result.subject)
        assertNull(result.to?.get(0))
        assertEquals("support@financesK.com", result.from)
    }

    @Test
    fun `sendMailToken should log success when email is sent successfully`() {
        // Given
        val testMessage = SimpleMailMessage().apply {
            setTo("user@example.com")
            subject = "Test Subject"
            text = "Test message body"
        }
        
        every { mockMailSender.send(any<SimpleMailMessage>()) } returns Unit

        // When
        mailService.sendMailToken(testMessage)

        // Then
        verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
    }

    private fun createTestUser(): AppUser {
        return AppUser(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashedpassword",
            passwordSalt = "salt",
            pathAvatar = "/avatars/test.jpg",
            createdAt = Instant.now()
        )
    }
}
