package io.red.financesK.user.service.update

import io.mockk.*
import io.red.financesK.auth.service.AuthService
import io.red.financesK.auth.service.PasswordService
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.mail.service.MailService
import io.red.financesK.user.controller.request.UpdateUserRequest
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

class UpdateUserServiceTest {

    private lateinit var updateUserService: UpdateUserService
    private val mockSearchUserService: SearchUserService = mockk(relaxed = true)
    private val mockUserRepository: AppUserRepository = mockk(relaxed = true)
    private val mockMailService: MailService = mockk(relaxed = true)
    private val mockAuthService: AuthService = mockk(relaxed = true)
    private val mockPasswordService: PasswordService = mockk(relaxed = true)
    private val mockHttpServletRequest: HttpServletRequest = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        updateUserService = UpdateUserService(
            mockSearchUserService,
            mockUserRepository,
            mockMailService,
            mockAuthService,
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
        val contextPath = "/app"
        val mailMessage = SimpleMailMessage()

        every { mockUserRepository.findByEmail(email) } returns user
        every { mockAuthService.getTokenFromUserId(user.id!!.toLong()) } returns token
        every { mockPasswordService.createPasswordResetTokenForUser(user, token) } returns token
        every { mockMailService.constructResetTokenEmail(contextPath, locale, token, user) } returns mailMessage
        every { mockMailService.sendMailToken(mailMessage) } just Runs
        every { mockHttpServletRequest.contextPath } returns contextPath
        every { mockHttpServletRequest.locale } returns locale

        // When
        val result = updateUserService.resetPassword(mockHttpServletRequest, email)

        // Then
        verify(exactly = 1) { mockUserRepository.findByEmail(email) }
        verify(exactly = 1) { mockAuthService.getTokenFromUserId(user.id!!.toLong()) }
        verify(exactly = 1) { mockPasswordService.createPasswordResetTokenForUser(user, token) }
        verify(exactly = 1) { mockMailService.constructResetTokenEmail(contextPath, locale, token, user) }
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
        verify(exactly = 0) { mockAuthService.getTokenFromUserId(any()) }
        verify(exactly = 0) { mockPasswordService.createPasswordResetTokenForUser(any(), any()) }
        verify(exactly = 0) { mockMailService.sendMailToken(any()) }
    }

    @Test
    @DisplayName("Deve salvar usuário corretamente")
    fun `should save user correctly`() {
        // Given
        val user = createTestUser()

        every { mockUserRepository.save(user) } returns user

        // When
        updateUserService.saveUser(user)

        // Then
        verify(exactly = 1) { mockUserRepository.save(user) }
    }

    @Test
    @DisplayName("Deve processar request com campos nulos")
    fun `should process request with null fields`() {
        // Given
        val userId = 1
        val originalUser = createTestUser()
        val originalEmail = originalUser.email
        val originalPathAvatar = originalUser.pathAvatar

        val updateRequest = UpdateUserRequest(
            username = "newusername",
            email = null,
            oldPassword = "oldpass",
            newPassword = "newpass",
            pathAvatar = null,
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
    @DisplayName("Deve processar reset de senha com diferentes locales")
    fun `should process password reset with different locales`() {
        // Given
        val email = "test@example.com"
        val user = createTestUser()
        val token = "reset-token-456"
        val locale = Locale.of("pt", "BR")
        val contextPath = "/financesK"
        val mailMessage = SimpleMailMessage()

        every { mockUserRepository.findByEmail(email) } returns user
        every { mockAuthService.getTokenFromUserId(user.id!!.toLong()) } returns token
        every { mockPasswordService.createPasswordResetTokenForUser(user, token) } returns token
        every { mockMailService.constructResetTokenEmail(contextPath, locale, token, user) } returns mailMessage
        every { mockMailService.sendMailToken(mailMessage) } just Runs
        every { mockHttpServletRequest.contextPath } returns contextPath
        every { mockHttpServletRequest.locale } returns locale

        // When
        val result = updateUserService.resetPassword(mockHttpServletRequest, email)

        // Then
        assertEquals("If the email is registered, a password reset link will be sent.", result.message)
        assertNotNull(result.locale)
    }

    @Test
    @DisplayName("Deve tratar usuário com ID nulo no reset de senha")
    fun `should handle user with null ID in password reset`() {
        // Given
        val email = "test@example.com"
        val user = createTestUserWithNullId()

        every { mockUserRepository.findByEmail(email) } returns user

        // When & Then
        assertThrows<NullPointerException> {
            updateUserService.resetPassword(mockHttpServletRequest, email)
        }

        verify(exactly = 1) { mockUserRepository.findByEmail(email) }
    }

    @Test
    @DisplayName("Deve atualizar apenas email quando outros campos estão vazios")
    fun `should update only email when other fields are empty`() {
        // Given
        val userId = 1
        val originalUser = createTestUser()
        val originalUsername = originalUser.username
        val originalPathAvatar = originalUser.pathAvatar

        val updateRequest = UpdateUserRequest(
            username = "",
            email = "onlyemail@example.com",
            oldPassword = "oldpass",
            newPassword = "newpass",
            pathAvatar = "",
            token = null
        )

        every { mockSearchUserService.findUserById(userId) } returns originalUser
        every { mockUserRepository.save(any()) } returns originalUser

        // When
        updateUserService.updateUser(userId, updateRequest)

        // Then
        assertEquals(originalUsername, originalUser.username)
        assertEquals("onlyemail@example.com", originalUser.email)
        assertEquals(originalPathAvatar, originalUser.pathAvatar)
    }

    @Test
    @DisplayName("Deve atualizar apenas pathAvatar quando outros campos estão vazios")
    fun `should update only pathAvatar when other fields are empty`() {
        // Given
        val userId = 1
        val originalUser = createTestUser()
        val originalUsername = originalUser.username
        val originalEmail = originalUser.email

        val updateRequest = UpdateUserRequest(
            username = "",
            email = "",
            oldPassword = "oldpass",
            newPassword = "newpass",
            pathAvatar = "/new/path/avatar.png",
            token = null
        )

        every { mockSearchUserService.findUserById(userId) } returns originalUser
        every { mockUserRepository.save(any()) } returns originalUser

        // When
        updateUserService.updateUser(userId, updateRequest)

        // Then
        assertEquals(originalUsername, originalUser.username)
        assertEquals(originalEmail, originalUser.email)
        assertEquals("/new/path/avatar.png", originalUser.pathAvatar)
    }

    @Test
    @DisplayName("Deve verificar que updatedAt é sempre atualizado")
    fun `should verify that updatedAt is always updated`() {
        // Given
        val userId = 1
        val originalUser = createTestUser()
        val beforeUpdate = Instant.now()

        val updateRequest = UpdateUserRequest(
            username = "",
            email = "",
            oldPassword = "oldpass",
            newPassword = "newpass",
            pathAvatar = "",
            token = null
        )

        every { mockSearchUserService.findUserById(userId) } returns originalUser
        every { mockUserRepository.save(any()) } returns originalUser

        // When
        updateUserService.updateUser(userId, updateRequest)

        // Then
        assertNotNull(originalUser.updatedAt)
        assertTrue(originalUser.updatedAt!!.isAfter(beforeUpdate) || originalUser.updatedAt!!.equals(beforeUpdate))
    }

    @Test
    @DisplayName("Deve processar corretamente quando email é string vazia")
    fun `should process correctly when email is empty string`() {
        // Given
        val userId = 1
        val originalUser = createTestUser()
        val originalEmail = originalUser.email

        val updateRequest = UpdateUserRequest(
            username = "newusername",
            email = "",
            oldPassword = "oldpass",
            newPassword = "newpass",
            pathAvatar = "/new/avatar.jpg",
            token = null
        )

        every { mockSearchUserService.findUserById(userId) } returns originalUser
        every { mockUserRepository.save(any()) } returns originalUser

        // When
        updateUserService.updateUser(userId, updateRequest)

        // Then
        assertEquals("newusername", originalUser.username)
        assertEquals(originalEmail, originalUser.email) // Should maintain original email
        assertEquals("/new/avatar.jpg", originalUser.pathAvatar)
    }

    private fun createTestUser(): AppUser {
        return AppUser(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashedpassword",
            passwordSalt = "salt123",
            pathAvatar = "/avatars/test.jpg",
            createdAt = Instant.now().minusSeconds(3600), // 1 hour ago
            updatedAt = Instant.now().minusSeconds(1800)  // 30 minutes ago
        )
    }

    private fun createTestUserWithNullId(): AppUser {
        return AppUser(
            id = null,
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
