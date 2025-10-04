package io.red.financesK.mail.service

import io.mockk.*
import io.red.financesK.user.model.AppUser
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.util.ReflectionTestUtils
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MailServiceTest {

    private lateinit var mailService: MailService
    private val mockMailSender: JavaMailSender = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        mailService = MailService(mockMailSender)
        
        // Setting up the @Value properties using ReflectionTestUtils
        ReflectionTestUtils.setField(mailService, "supportEmail", "support@financesK.com")
        ReflectionTestUtils.setField(mailService, "baseUrl", "http://localhost:8080")

        clearAllMocks()
    }

    @Test
    @DisplayName("constructResetTokenEmail should return properly formatted email")
    fun `constructResetTokenEmail should return properly formatted email`() {
        // Given
        val token = "test-reset-token-123"
        val user = createTestUser()

        // When
        val result = mailService.constructResetTokenEmail(token, user)

        // Then
        assertNotNull(result)
        assertEquals("Password Reset", result.subject)
        assertEquals("test@example.com", result.to?.get(0))
        assertEquals("support@financesK.com", result.from)

        val expectedUrl = "http://localhost:8080/api/v1/users/change-password?token=$token"
        val expectedMessage = "Reset your password using the following link: $expectedUrl"
        val expectedText = "$expectedMessage \r\n$expectedUrl"
        assertEquals(expectedText, result.text)
    }

    @Test
    @DisplayName("constructResetTokenEmail should use configured base URL")
    fun `constructResetTokenEmail should use configured base URL`() {
        // Given
        ReflectionTestUtils.setField(mailService, "baseUrl", "https://financesK.com")
        val token = "test-token"
        val user = createTestUser()

        // When
        val result = mailService.constructResetTokenEmail(token, user)

        // Then
        val expectedUrl = "https://financesK.com/api/v1/users/change-password?token=$token"
        val expectedMessage = "Reset your password using the following link: $expectedUrl"
        val expectedText = "$expectedMessage \r\n$expectedUrl"
        assertEquals(expectedText, result.text)
    }

    @Test
    @DisplayName("constructResetTokenEmail should work with different base URLs")
    fun `constructResetTokenEmail should work with different base URLs`() {
        // Given
        ReflectionTestUtils.setField(mailService, "baseUrl", "https://app.financesK.io")
        val token = "another-test-token"
        val user = createTestUser()

        // When
        val result = mailService.constructResetTokenEmail(token, user)

        // Then
        val expectedUrl = "https://app.financesK.io/api/v1/users/change-password?token=$token"
        val expectedMessage = "Reset your password using the following link: $expectedUrl"
        val expectedText = "$expectedMessage \r\n$expectedUrl"
        assertEquals(expectedText, result.text)
        assertEquals("Password Reset", result.subject)
        assertEquals("test@example.com", result.to?.get(0))
    }

    @Test
    @DisplayName("constructResetTokenEmail should include both message and URL in body")
    fun `constructResetTokenEmail should include both message and URL in body`() {
        // Given
        val token = "body-test-token"
        val user = createTestUser()

        // When
        val result = mailService.constructResetTokenEmail(token, user)

        // Then
        val expectedUrl = "http://localhost:8080/api/v1/users/change-password?token=$token"

        // Verify the message contains both the descriptive text and the URL
        assertNotNull(result.text)
        assertEquals(true, result.text!!.contains("Reset your password using the following link:"))
        assertEquals(true, result.text!!.contains(expectedUrl))
        assertEquals(true, result.text!!.contains("\r\n"))

        // Verify the complete expected format
        val expectedMessage = "Reset your password using the following link: $expectedUrl"
        val expectedText = "$expectedMessage \r\n$expectedUrl"
        assertEquals(expectedText, result.text)
    }

    @Test
    @DisplayName("sendMailToken should call JavaMailSender")
    fun `sendMailToken should call JavaMailSender`() {
        // Given
        val message = SimpleMailMessage().apply {
            setTo("test@example.com")
            subject = "Test Subject"
            text = "Test body"
            from = "support@financesK.com"
        }

        every { mockMailSender.send(message) } just Runs

        // When
        mailService.sendMailToken(message)

        // Then
        verify(exactly = 1) { mockMailSender.send(message) }
    }

    @Test
    @DisplayName("constructWelcomeEmail should return properly formatted welcome email")
    fun `constructWelcomeEmail should return properly formatted welcome email`() {
        // Given
        val user = createTestUser()

        // When
        val result = mailService.constructWelcomeEmail(user)

        // Then
        assertNotNull(result)
        assertEquals("Welcome to FinancesK", result.subject)
        assertEquals("Welcome to FinancesK! Your account has been successfully created.", result.text)
        assertEquals("test@example.com", result.to?.get(0))
        assertEquals("support@financesK.com", result.from)
    }

    @Test
    @DisplayName("constructGenericEmail should return properly formatted generic email")
    fun `constructGenericEmail should return properly formatted generic email`() {
        // Given
        val user = createTestUser()
        val subject = "Test Subject"
        val body = "Test message body"

        // When
        val result = mailService.constructGenericEmail(subject, body, user)

        // Then
        assertNotNull(result)
        assertEquals(subject, result.subject)
        assertEquals(body, result.text)
        assertEquals("test@example.com", result.to?.get(0))
        assertEquals("support@financesK.com", result.from)
    }

    @Test
    @DisplayName("sendMailToken should throw exception when JavaMailSender fails")
    fun `sendMailToken should throw exception when JavaMailSender fails`() {
        // Given
        val message = SimpleMailMessage().apply {
            setTo("test@example.com")
            subject = "Test Subject"
            text = "Test body"
        }
        val expectedException = RuntimeException("Mail server error")

        every { mockMailSender.send(message) } throws expectedException

        // When & Then
        val thrownException = org.junit.jupiter.api.assertThrows<RuntimeException> {
            mailService.sendMailToken(message)
        }
        assertEquals("Mail server error", thrownException.message)
        verify(exactly = 1) { mockMailSender.send(message) }
    }

    private fun createTestUser(): AppUser {
        return AppUser(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashedpassword",
            passwordSalt = "salt123",
            pathAvatar = "/avatars/test.jpg",
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
