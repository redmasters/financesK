package io.red.financesK.user.service.update

import io.mockk.*
import io.red.financesK.auth.jwt.JwtTokenProvider
import io.red.financesK.auth.service.CustomUserDetails
import io.red.financesK.auth.service.PasswordService
import io.red.financesK.auth.service.UserDetailsServiceImpl
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.mail.service.MailService
import io.red.financesK.user.controller.request.UpdateUserRequest
import io.red.financesK.user.controller.response.GenericResponse
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import io.red.financesK.user.service.search.SearchUserService
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mail.SimpleMailMessage
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class UpdateUserServiceTest {

    private lateinit var updateUserService: UpdateUserService
    private val mockSearchUserService: SearchUserService = mockk(relaxed = true)
    private val mockUserRepository: AppUserRepository = mockk(relaxed = true)
    private val mockMailService: MailService = mockk(relaxed = true)
    private val mockJwtTokenProvider: JwtTokenProvider = mockk(relaxed = true)
    private val mockUserDetailsService: UserDetailsServiceImpl = mockk(relaxed = true)
    private val mockPasswordService: PasswordService = mockk(relaxed = true)
    private val mockHttpServletRequest: HttpServletRequest = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        updateUserService = UpdateUserService(
            mockSearchUserService,
            mockUserRepository,
            mockMailService,
            mockJwtTokenProvider,
            mockUserDetailsService,
            mockPasswordService
        )
        clearAllMocks()
    }

    @Test
    @DisplayName("Deve atualizar usuário com todos os campos preenchidos")
    fun `should update user with all fields filled`() {
        // Given
        val userId = 1
        val originalUser = createTestUser()
        val updateRequest = UpdateUserRequest(
            username = "newusername",
            email = "newemail@example.com",
            oldPassword = "oldpass",
            newPassword = "newpass",
            confirmPassword = "newpass",
            pathAvatar = "/new/avatar/path.jpg",
            token = null
        )

        every { mockSearchUserService.findUserById(userId) } returns originalUser
        every { mockUserRepository.save(any()) } returns originalUser

        // When
        updateUserService.updateUser(userId, updateRequest)

        // Then
        verify(exactly = 1) { mockSearchUserService.findUserById(userId) }
        verify(exactly = 1) { mockUserRepository.save(any()) }

        assertEquals("newusername", originalUser.username)
        assertEquals("newemail@example.com", originalUser.email)
        assertEquals("/new/avatar/path.jpg", originalUser.pathAvatar)
        assertNotNull(originalUser.updatedAt)
    }

    @Test
    @DisplayName("Deve manter valores originais quando campos estão vazios")
    fun `should keep original values when fields are empty`() {
        // Given
        val userId = 1
        val originalUser = createTestUser()
        val originalUsername = originalUser.username
        val originalEmail = originalUser.email
        val originalPathAvatar = originalUser.pathAvatar

        val updateRequest = UpdateUserRequest(
            username = "",
            email = "",
            oldPassword = "oldpass",
            newPassword = "newpass",
            confirmPassword = "newpass",
            pathAvatar = "",
            token = null
        )

        every { mockSearchUserService.findUserById(userId) } returns originalUser
        every { mockUserRepository.save(any()) } returns originalUser

        // When
        updateUserService.updateUser(userId, updateRequest)

        // Then
        verify(exactly = 1) { mockSearchUserService.findUserById(userId) }
        verify(exactly = 1) { mockUserRepository.save(any()) }

        assertEquals(originalUsername, originalUser.username)
        assertEquals(originalEmail, originalUser.email)
        assertEquals(originalPathAvatar, originalUser.pathAvatar)
    }

    @Test
    @DisplayName("Deve atualizar apenas campos não vazios")
    fun `should update only non-empty fields`() {
        // Given
        val userId = 1
        val originalUser = createTestUser()
        val originalEmail = originalUser.email
        val originalPathAvatar = originalUser.pathAvatar

        val updateRequest = UpdateUserRequest(
            username = "newusername",
            email = "",
            oldPassword = "oldpass",
            newPassword = "newpass",
            confirmPassword = "newpass",
            pathAvatar = "",
            token = null
        )

        every { mockSearchUserService.findUserById(userId) } returns originalUser
        every { mockUserRepository.save(any()) } returns originalUser

        // When
        updateUserService.updateUser(userId, updateRequest)

        // Then
        assertEquals("newusername", originalUser.username)
        assertEquals(originalEmail, originalUser.email)
        assertEquals(originalPathAvatar, originalUser.pathAvatar)
    }

    @Test
    @DisplayName("Deve atualizar updatedAt ao atualizar usuário")
    fun `should update updatedAt when updating user`() {
        // Given
        val userId = 1
        val originalUser = createTestUser()
        val originalUpdatedAt = originalUser.updatedAt

        val updateRequest = UpdateUserRequest(
            username = "newusername",
            email = "newemail@example.com",
            oldPassword = "oldpass",
            newPassword = "newpass",
            confirmPassword = "newpass",
            pathAvatar = "/new/avatar.jpg",
            token = null
        )

        every { mockSearchUserService.findUserById(userId) } returns originalUser
        every { mockUserRepository.save(any()) } returns originalUser

        // When
        updateUserService.updateUser(userId, updateRequest)

        // Then
        assertTrue(originalUser.updatedAt!!.isAfter(originalUpdatedAt!!))
    }

    @Test
    @DisplayName("Deve resetar senha com email válido")
    fun `should reset password with valid email`() {
        // Given
        val email = "test@example.com"
        val user = createTestUser()
        val token = "reset-token-123"
        val locale = Locale.ENGLISH
        val mailMessage = SimpleMailMessage()
        val customUserDetails = mockk<CustomUserDetails>()

        every { mockUserRepository.findByEmail(email) } returns user
        every { mockUserDetailsService.loadUserByUsername(user.username!!) } returns customUserDetails
        every { mockJwtTokenProvider.generateToken(customUserDetails) } returns token
        every { mockPasswordService.createPasswordResetTokenForUser(user, token) } returns token
        every { mockMailService.constructResetTokenEmail(token, user) } returns mailMessage
        every { mockMailService.sendMailToken(mailMessage) } just Runs
        every { mockHttpServletRequest.locale } returns locale

        // When
        val result = updateUserService.resetPassword(mockHttpServletRequest, email)

        // Then
        verify(exactly = 1) { mockUserRepository.findByEmail(email) }
        verify(exactly = 1) { mockUserDetailsService.loadUserByUsername(user.username!!) }
        verify(exactly = 1) { mockJwtTokenProvider.generateToken(customUserDetails) }
        verify(exactly = 1) { mockPasswordService.createPasswordResetTokenForUser(user, token) }
        verify(exactly = 1) { mockMailService.constructResetTokenEmail(token, user) }
        verify(exactly = 1) { mockMailService.sendMailToken(mailMessage) }

        assertEquals("If the email is registered, a password reset link will be sent.", result.message)
        assertNotNull(result.locale)
    }

    @Test
    @DisplayName("Deve lançar exceção para email não encontrado no reset de senha")
    fun `should throw exception for email not found in password reset`() {
        // Given
        val email = "notfound@example.com"

        every { mockUserRepository.findByEmail(email) } returns null

        // When & Then
        val exception = assertThrows<ValidationException> {
            updateUserService.resetPassword(mockHttpServletRequest, email)
        }

        assertEquals("User with email $email not found", exception.message)
        verify(exactly = 1) { mockUserRepository.findByEmail(email) }
        verify(exactly = 0) { mockUserDetailsService.loadUserByUsername(any()) }
        verify(exactly = 0) { mockJwtTokenProvider.generateToken(any()) }
        verify(exactly = 0) { mockMailService.sendMailToken(any()) }
    }

    @Test
    @DisplayName("Deve validar token corretamente")
    fun `should validate token correctly`() {
        // Given
        val validToken = "valid-token"
        val invalidToken = "invalid-token"

        every { mockPasswordService.validatePasswordResetToken(validToken) } returns true
        every { mockPasswordService.validatePasswordResetToken(invalidToken) } returns false

        // When
        val validResult = updateUserService.changePassword(validToken)
        val invalidResult = updateUserService.changePassword(invalidToken)

        // Then
        assertTrue(validResult)
        assertFalse(invalidResult)
        verify { mockPasswordService.validatePasswordResetToken(validToken) }
        verify { mockPasswordService.validatePasswordResetToken(invalidToken) }
    }

    @Test
    @DisplayName("Deve salvar senha através do passwordService")
    fun `should save password through passwordService`() {
        // Given
        val locale = Locale.ENGLISH
        val request = UpdateUserRequest(
            username = "testuser",
            email = "test@example.com",
            oldPassword = "oldpass",
            newPassword = "newpass",
            confirmPassword = "newpass",
            pathAvatar = "/avatar.jpg",
            token = "valid-token"
        )
        val expectedResponse = GenericResponse("Success", null, locale)

        every { mockPasswordService.savePassword(locale, request) } returns expectedResponse

        // When
        val result = updateUserService.savePassword(locale, request)

        // Then
        assertEquals(expectedResponse, result)
        verify { mockPasswordService.savePassword(locale, request) }
    }

    private fun createTestUser(): AppUser {
        return AppUser(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashedpassword",
            passwordSalt = "salt123",
            pathAvatar = "/avatars/test.jpg",
            createdAt = Instant.now().minusSeconds(3600),
            updatedAt = Instant.now().minusSeconds(1800)
        )
    }
}
