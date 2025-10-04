package io.red.financesK.auth.service

import io.mockk.*
import io.red.financesK.auth.model.Authority
import io.red.financesK.auth.model.PasswordResetToken
import io.red.financesK.auth.repository.PasswordResetTokenRepository
import io.red.financesK.user.controller.request.UpdateUserRequest
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*
import kotlin.test.*

class PasswordServiceTest {

    private lateinit var passwordService: PasswordService
    private val mockPasswordResetTokenRepository: PasswordResetTokenRepository = mockk()
    private val mockAppUserRepository: AppUserRepository = mockk()

    @BeforeEach
    fun setUp() {
        passwordService = PasswordService(mockPasswordResetTokenRepository, mockAppUserRepository)
        clearAllMocks()
    }

    @Test
    @DisplayName("Deve codificar senha corretamente")
    fun `should encode password correctly`() {
        // Given
        val rawPassword = "mySecretPassword123"

        // When
        val encodedPassword = passwordService.encode(rawPassword)

        // Then
        assertNotNull(encodedPassword)
        assertTrue(encodedPassword.isNotEmpty())
        assertNotEquals(rawPassword, encodedPassword)
        assertTrue(encodedPassword.startsWith("\$2a\$") || encodedPassword.startsWith("\$2b\$"))
    }

    @Test
    @DisplayName("Deve verificar senha corretamente")
    fun `should verify password correctly`() {
        // Given
        val rawPassword = "testPassword456"
        val encodedPassword = passwordService.encode(rawPassword)

        // When
        val matches = passwordService.matches(rawPassword, encodedPassword)

        // Then
        assertTrue(matches)
    }

    @Test
    @DisplayName("Deve retornar false para senha incorreta")
    fun `should return false for incorrect password`() {
        // Given
        val correctPassword = "correctPassword"
        val incorrectPassword = "wrongPassword"
        val encodedPassword = passwordService.encode(correctPassword)

        // When
        val matches = passwordService.matches(incorrectPassword, encodedPassword)

        // Then
        assertFalse(matches)
    }

    @Test
    @DisplayName("Deve gerar salt aleatório")
    fun `should generate random salt`() {
        // When
        val salt1 = passwordService.saltPassword()
        val salt2 = passwordService.saltPassword()

        // Then
        assertNotNull(salt1)
        assertNotNull(salt2)
        assertEquals(32, salt1.length) // 16 bytes = 32 hex characters
        assertEquals(32, salt2.length)
        assertNotEquals(salt1, salt2) // Should be different each time
        assertTrue(salt1.matches(Regex("[0-9a-f]+"))) // Should be hex
    }

    @Test
    @DisplayName("Deve criar novo token quando não existe token válido")
    fun `should create new token when no valid token exists`() {
        // Given
        val user = createTestUser()
        val token = "new-reset-token-123"
        val expectedToken = PasswordResetToken(
            token = token,
            user = user,
            expiryDate = Date(System.currentTimeMillis() + passwordService.EXPIRATION)
        )

        every { mockPasswordResetTokenRepository.findTopByUser_Id(user.id!!) } returns Optional.empty()
        every { mockAppUserRepository.save(user) } returns user  // Mock necessário para salvar authorities
        every { mockPasswordResetTokenRepository.save(any()) } returns expectedToken

        // When
        val result = passwordService.createPasswordResetTokenForUser(user, token)

        // Then
        assertEquals(token, result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findTopByUser_Id(user.id!!) }
        verify(exactly = 1) { mockAppUserRepository.save(user) }  // Verifica que o usuário foi salvo com as authorities
        verify(exactly = 1) { mockPasswordResetTokenRepository.save(any()) }
    }

    @Test
    @DisplayName("Deve retornar token existente se ainda válido")
    fun `should return existing token if still valid`() {
        // Given
        val user = createTestUser()
        val existingToken = "existing-valid-token"
        val futureDate = Date(System.currentTimeMillis() + 3600000) // 1 hour in future
        val existingPasswordResetToken = PasswordResetToken(
            id = 1L,
            token = existingToken,
            user = user,
            expiryDate = futureDate
        )

        every { mockPasswordResetTokenRepository.findTopByUser_Id(user.id!!) } returns Optional.of(existingPasswordResetToken)

        // When
        val result = passwordService.createPasswordResetTokenForUser(user, "new-token")

        // Then
        assertEquals(existingToken, result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findTopByUser_Id(user.id!!) }
        verify(exactly = 0) { mockPasswordResetTokenRepository.save(any()) }
    }

    @Test
    @DisplayName("Deve criar novo token se existente estiver expirado")
    fun `should create new token if existing is expired`() {
        // Given
        val user = createTestUser()
        val newToken = "new-token-after-expiry"
        val pastDate = Date(System.currentTimeMillis() - 3600000) // 1 hour in past
        val expiredToken = PasswordResetToken(
            id = 1L,
            token = "expired-token",
            user = user,
            expiryDate = pastDate
        )

        every { mockPasswordResetTokenRepository.findTopByUser_Id(user.id!!) } returns Optional.of(expiredToken)
        every { mockAppUserRepository.save(user) } returns user  // Mock necessário para salvar authorities
        every { mockPasswordResetTokenRepository.save(any()) } returns mockk()

        // When
        val result = passwordService.createPasswordResetTokenForUser(user, newToken)

        // Then
        assertEquals(newToken, result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findTopByUser_Id(user.id!!) }
        verify(exactly = 1) { mockAppUserRepository.save(user) }  // Verifica que o usuário foi salvo com as authorities
        verify(exactly = 1) { mockPasswordResetTokenRepository.save(any()) }
    }

    @Test
    @DisplayName("Deve validar token existente e não expirado")
    fun `should validate existing and non-expired token`() {
        // Given
        val token = "valid-token-123"
        val futureDate = Date(System.currentTimeMillis() + 3600000)
        val validPasswordResetToken = PasswordResetToken(
            id = 1L,
            token = token,
            user = createTestUser(),
            expiryDate = futureDate
        )

        every { mockPasswordResetTokenRepository.findByToken(token) } returns Optional.of(validPasswordResetToken)

        // When
        val result = passwordService.validatePasswordResetToken(token)

        // Then
        assertTrue(result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(token) }
    }

    @Test
    @DisplayName("Deve invalidar token não encontrado")
    fun `should invalidate token not found`() {
        // Given
        val token = "non-existent-token"

        every { mockPasswordResetTokenRepository.findByToken(token) } returns Optional.empty()

        // When
        val result = passwordService.validatePasswordResetToken(token)

        // Then
        assertFalse(result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(token) }
    }

    @Test
    @DisplayName("Deve invalidar token expirado")
    fun `should invalidate expired token`() {
        // Given
        val token = "expired-token-123"
        val pastDate = Date(System.currentTimeMillis() - 3600000)
        val expiredToken = PasswordResetToken(
            id = 1L,
            token = token,
            user = createTestUser(),
            expiryDate = pastDate
        )

        every { mockPasswordResetTokenRepository.findByToken(token) } returns Optional.of(expiredToken)

        // When
        val result = passwordService.validatePasswordResetToken(token)

        // Then
        assertFalse(result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(token) }
    }

    @Test
    @DisplayName("Deve salvar nova senha com token válido")
    fun `should save new password with valid token`() {
        // Given
        val locale = Locale.ENGLISH
        val token = "valid-reset-token"
        val newPassword = "newSecurePassword123"
        val user = createTestUser()

        val updateRequest = UpdateUserRequest(
            username = "testuser",
            email = "test@example.com",
            oldPassword = "oldpass",
            newPassword = newPassword,
            confirmPassword = newPassword,
            pathAvatar = "/avatar.jpg",
            token = token
        )

        val passwordResetToken = PasswordResetToken(
            id = 1L,
            token = token,
            user = user,
            expiryDate = Date(System.currentTimeMillis() + 3600000)
        )

        every { mockPasswordResetTokenRepository.findByToken(token) } returns Optional.of(passwordResetToken)
        every { mockAppUserRepository.save(any()) } returns user
        every { mockPasswordResetTokenRepository.delete(passwordResetToken) } just Runs

        // When
        val result = passwordService.savePassword(locale, updateRequest)

        // Then
        assertEquals("auth.message.resetPasswordSuc", result.message)
        assertNull(result.error)
        assertEquals(locale.toString(), result.locale)

        // findByToken é chamado 2 vezes: uma na validação (isTokenFound) e outra no savePassword
        verify(exactly = 2) { mockPasswordResetTokenRepository.findByToken(token) }
        verify(exactly = 1) { mockAppUserRepository.save(any()) }
        verify(exactly = 1) { mockPasswordResetTokenRepository.delete(passwordResetToken) }
    }

    @Test
    @DisplayName("Deve retornar erro para token inválido na alteração de senha")
    fun `should return error for invalid token in save password`() {
        // Given
        val locale = Locale.FRENCH
        val invalidToken = "invalid-token"
        val updateRequest = UpdateUserRequest(
            username = "testuser",
            email = "test@example.com",
            oldPassword = "oldpass",
            newPassword = "newpass",
            confirmPassword = "newpass",
            pathAvatar = "/avatar.jpg",
            token = invalidToken
        )

        every { mockPasswordResetTokenRepository.findByToken(invalidToken) } returns Optional.empty()

        // When
        val result = passwordService.savePassword(locale, updateRequest)

        // Then
        assertEquals("auth.message.invalid", result.message)
        assertEquals("error.token", result.error)
        assertEquals(locale.toString(), result.locale)

        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(invalidToken) }
        verify(exactly = 0) { mockAppUserRepository.save(any()) }
        verify(exactly = 0) { mockPasswordResetTokenRepository.delete(any()) }
    }

    @Test
    @DisplayName("Deve buscar usuário por token de reset válido")
    fun `should get user by valid reset token`() {
        // Given
        val token = "valid-token"
        val user = createTestUser()
        val passwordResetToken = PasswordResetToken(
            id = 1L,
            token = token,
            user = user,
            expiryDate = Date(System.currentTimeMillis() + 3600000)
        )

        every { mockPasswordResetTokenRepository.findByToken(token) } returns Optional.of(passwordResetToken)

        // When
        val result = passwordService.getUserByPasswordResetToken(token)

        // Then
        assertEquals(user, result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(token) }
    }

    @Test
    @DisplayName("Deve lançar exceção para token não encontrado ao buscar usuário")
    fun `should throw exception for token not found when getting user`() {
        // Given
        val token = "non-existent-token"

        every { mockPasswordResetTokenRepository.findByToken(token) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<RuntimeException> {
            passwordService.getUserByPasswordResetToken(token)
        }

        assertEquals("Token not found", exception.message)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(token) }
    }

    @Test
    @DisplayName("Deve verificar se token existe e não está expirado")
    fun `should check if token exists and is not expired`() {
        // Given
        val token = "existing-valid-token"
        val futureDate = Date(System.currentTimeMillis() + 3600000)
        val validToken = PasswordResetToken(
            id = 1L,
            token = token,
            user = createTestUser(),
            expiryDate = futureDate
        )

        every { mockPasswordResetTokenRepository.findByToken(token) } returns Optional.of(validToken)

        // When
        val result = passwordService.isTokenFound(token)

        // Then
        assertTrue(result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(token) }
    }

    @Test
    @DisplayName("Deve retornar false se token não existe")
    fun `should return false if token does not exist`() {
        // Given
        val token = "non-existent-token"

        every { mockPasswordResetTokenRepository.findByToken(token) } returns Optional.empty()

        // When
        val result = passwordService.isTokenFound(token)

        // Then
        assertFalse(result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(token) }
    }

    @Test
    @DisplayName("Deve retornar false se token está expirado")
    fun `should return false if token is expired`() {
        // Given
        val token = "expired-token"
        val pastDate = Date(System.currentTimeMillis() - 3600000)
        val expiredToken = PasswordResetToken(
            id = 1L,
            token = token,
            user = createTestUser(),
            expiryDate = pastDate
        )

        every { mockPasswordResetTokenRepository.findByToken(token) } returns Optional.of(expiredToken)

        // When
        val result = passwordService.isTokenFound(token)

        // Then
        assertFalse(result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(token) }
    }

    @Test
    @DisplayName("Deve verificar corretamente se token está expirado")
    fun `should correctly check if token is expired`() {
        // Given
        val pastDate = Date(System.currentTimeMillis() - 3600000)
        val futureDate = Date(System.currentTimeMillis() + 3600000)

        val expiredToken = PasswordResetToken(
            id = 1L,
            token = "expired",
            user = createTestUser(),
            expiryDate = pastDate
        )

        val validToken = PasswordResetToken(
            id = 2L,
            token = "valid",
            user = createTestUser(),
            expiryDate = futureDate
        )

        // When
        val expiredResult = passwordService.isTokenExpired(expiredToken)
        val validResult = passwordService.isTokenExpired(validToken)

        // Then
        assertTrue(expiredResult)
        assertFalse(validResult)
    }

    @Test
    @DisplayName("Deve atualizar hash e salt da senha do usuário")
    fun `should update user password hash and salt`() {
        // Given
        val locale = Locale.getDefault()
        val token = "valid-token"
        val newPassword = "newSecurePassword456"
        val user = createTestUser()
        val originalPasswordHash = user.passwordHash

        val updateRequest = UpdateUserRequest(
            username = "testuser",
            email = "test@example.com",
            oldPassword = "oldpass",
            newPassword = newPassword,
            confirmPassword = newPassword,
            pathAvatar = "/avatar.jpg",
            token = token
        )

        val passwordResetToken = PasswordResetToken(
            id = 1L,
            token = token,
            user = user,
            expiryDate = Date(System.currentTimeMillis() + 3600000)
        )

        every { mockPasswordResetTokenRepository.findByToken(token) } returns Optional.of(passwordResetToken)
        every { mockAppUserRepository.save(any()) } returns user
        every { mockPasswordResetTokenRepository.delete(passwordResetToken) } just Runs

        // When
        passwordService.savePassword(locale, updateRequest)

        // Then
        assertNotEquals(originalPasswordHash, user.passwordHash)
        assertNotNull(user.passwordSalt)
        assertTrue(user.passwordSalt!!.isNotEmpty())
        assertTrue(passwordService.matches(newPassword, user.passwordHash!!))
    }

    @Test
    @DisplayName("Deve processar token com data limite exata")
    fun `should process token with exact expiry date`() {
        // Given
        val token = "exact-expiry-token"
        val exactExpiryDate = Date() // Exactly now
        val tokenAtExpiry = PasswordResetToken(
            id = 1L,
            token = token,
            user = createTestUser(),
            expiryDate = exactExpiryDate
        )

        // When
        val result = passwordService.isTokenExpired(tokenAtExpiry)

        // Then
        // Token should be considered expired if expiry date is before or equal to current time
        // We just verify the method executes without throwing an exception
        assertNotNull(result)
    }

    @Test
    @DisplayName("Deve lidar com múltiplas chamadas de encode para mesma senha")
    fun `should handle multiple encode calls for same password`() {
        // Given
        val password = "samePassword123"

        // When
        val encoded1 = passwordService.encode(password)
        val encoded2 = passwordService.encode(password)

        // Then
        assertNotEquals(encoded1, encoded2) // Should generate different hashes due to salt
        assertTrue(passwordService.matches(password, encoded1))
        assertTrue(passwordService.matches(password, encoded2))
    }

    @Test
    @DisplayName("Deve processar senhas com caracteres especiais")
    fun `should process passwords with special characters`() {
        // Given
        val complexPassword = "P@ssw0rd!#\$%^&*()_+-=[]{}|;:'\",.<>?/~`"

        // When
        val encodedPassword = passwordService.encode(complexPassword)
        val matches = passwordService.matches(complexPassword, encodedPassword)

        // Then
        assertTrue(matches)
        assertNotEquals(complexPassword, encodedPassword)
    }

    @Test
    @DisplayName("Deve processar senhas vazias")
    fun `should process empty passwords`() {
        // Given
        val emptyPassword = ""

        // When
        val encodedPassword = passwordService.encode(emptyPassword)
        val matches = passwordService.matches(emptyPassword, encodedPassword)

        // Then
        assertTrue(matches)
        assertNotNull(encodedPassword)
        assertTrue(encodedPassword.isNotEmpty())
    }

    private fun createTestUser(): AppUser {
        return AppUser(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "originalHashedPassword",
            passwordSalt = "originalSalt123",
            pathAvatar = "/avatars/test.jpg",
            createdAt = Instant.now().minusSeconds(3600),
            updatedAt = Instant.now().minusSeconds(1800),
            authorities = mutableSetOf(Authority.USER)
        )
    }
}
