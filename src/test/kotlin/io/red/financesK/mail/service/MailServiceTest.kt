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
        ReflectionTestUtils.setField(mailService, "host", "smtp.gmail.com")
        ReflectionTestUtils.setField(mailService, "port", 587)
        ReflectionTestUtils.setField(mailService, "username", "test@financesK.com")
        ReflectionTestUtils.setField(mailService, "password", "testpassword")
    }

    @Test
    fun `sendMailToken should send email with correct parameters`() {
        // Given
        val testMessage = SimpleMailMessage().apply {
            setTo("user@example.com")
            subject = "Test Subject"
            text = "Test message body"
        }
        
        val messageSlot = slot<SimpleMailMessage>()
        every { mockMailSender.send(capture(messageSlot)) } returns Unit

        // When
        mailService.sendMailToken(testMessage)

        // Then
        verify(exactly = 1) { mockMailSender.send(testMessage) }
        assertTrue(messageSlot.captured === testMessage)
    }

    @Test
    fun `constructResetTokenEmail should return properly formatted email`() {
        // Given
        val contextPath = "https://financesK.com"
        val locale = Locale.ENGLISH
        val token = "test-token-123"
        val user = createTestUser()
        val expectedUrl = "$contextPath/user/change-password?token=$token"

        // When
        val result = mailService.constructResetTokenEmail(contextPath, locale, token, user)

        // Then
        assertEquals("Password Reset", result.subject)
        assertEquals("test@example.com", result.to?.get(0))
        assertEquals("support@financesK.com", result.from)
        assertTrue(result.text!!.contains("Reset your password using the following link"))
        assertTrue(result.text!!.contains(expectedUrl))
        assertTrue(result.text!!.contains(locale.toString()))
    }

    @Test
    fun `constructResetTokenEmail should handle empty context path`() {
        // Given
        val contextPath = ""
        val locale = Locale.FRENCH
        val token = "empty-context-token"
        val user = createTestUser()

        // When
        val result = mailService.constructResetTokenEmail(contextPath, locale, token, user)

        // Then
        assertEquals("Password Reset", result.subject)
        assertTrue(result.text!!.contains("/user/change-password?token=$token"))
        assertTrue(result.text!!.contains(locale.toString()))
    }

    @Test
    fun `constructResetTokenEmail should handle special characters in token`() {
        // Given
        val contextPath = "https://financesK.com"
        val locale = Locale.GERMAN
        val token = "special-token-!@#$%^&*()"
        val user = createTestUser()

        // When
        val result = mailService.constructResetTokenEmail(contextPath, locale, token, user)

        // Then
        assertEquals("Password Reset", result.subject)
        assertTrue(result.text!!.contains("change-password?token=$token"))
    }

    @Test
    fun `constructResetTokenEmail should work with different locales`() {
        // Given
        val contextPath = "https://financesK.com"
        val token = "locale-test-token"
        val user = createTestUser()

        val locales = listOf(
            Locale.ENGLISH,
            Locale.FRENCH,
            Locale.GERMAN,
            Locale.ITALIAN,
            Locale.Builder().setLanguage("pt").setRegion("BR").build() // Portuguese Brazil
        )

        // When & Then
        locales.forEach { locale ->
            val result = mailService.constructResetTokenEmail(contextPath, locale, token, user)
            
            assertEquals("Password Reset", result.subject)
            assertEquals("test@example.com", result.to?.get(0))
            assertTrue(result.text!!.contains(locale.toString()))
        }
    }

    @Test
    fun `getMailSender should return configured JavaMailSender`() {
        // When
        val result = mailService.getMailSender()

        // Then
        assertNotNull(result)
        // Note: Since JavaMailSenderImpl properties are not easily accessible for testing,
        // we just verify that the method returns a non-null sender
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
        val contextPath = "https://financesK.com"
        val locale = Locale.ENGLISH
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
            val result = mailService.constructResetTokenEmail(contextPath, locale, token, user)
            
            assertEquals("Password Reset", result.subject)
            assertEquals(email, result.to?.get(0))
            assertEquals("support@financesK.com", result.from)
        }
    }

    @Test
    fun `constructResetTokenEmail should include both message and URL in body`() {
        // Given
        val contextPath = "https://financesK.com"
        val locale = Locale.ENGLISH
        val token = "body-content-test"
        val user = createTestUser()
        val expectedUrl = "$contextPath/user/change-password?token=$token"

        // When
        val result = mailService.constructResetTokenEmail(contextPath, locale, token, user)

        // Then
        val bodyParts = result.text!!.split(" \r\n")
        assertEquals(2, bodyParts.size)
        assertTrue(bodyParts[0].contains("Reset your password using the following link"))
        assertTrue(bodyParts[0].contains(locale.toString()))
        assertEquals(expectedUrl, bodyParts[1])
    }

    @Test
    fun `constructResetTokenEmail should handle null user email`() {
        // Given
        val contextPath = "https://financesK.com"
        val locale = Locale.ENGLISH
        val token = "null-email-test"
        val user = createTestUser().copy(email = null)

        // When
        val result = mailService.constructResetTokenEmail(contextPath, locale, token, user)

        // Then
        assertEquals("Password Reset", result.subject)
        assertNull(result.to?.get(0))
        assertEquals("support@financesK.com", result.from)
    }

    @Test
    fun `sendMailToken should create internal message but send original`() {
        // Given
        val originalMessage = SimpleMailMessage().apply {
            setTo("recipient@example.com")
            subject = "Original Subject"
            text = "Original text"
        }
        
        val messageSlot = slot<SimpleMailMessage>()
        every { mockMailSender.send(capture(messageSlot)) } returns Unit

        // When
        mailService.sendMailToken(originalMessage)

        // Then
        verify(exactly = 1) { mockMailSender.send(originalMessage) }
        assertEquals(originalMessage, messageSlot.captured)
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
