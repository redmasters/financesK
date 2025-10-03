package io.red.financesK.auth.service

import io.mockk.*
import io.red.financesK.auth.model.PasswordResetToken
import io.red.financesK.auth.repository.PasswordResetTokenRepository
import io.red.financesK.user.controller.request.UpdateUserRequest
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.service.update.UpdateUserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PasswordServiceTest {

    private lateinit var passwordService: PasswordService
    private val mockPasswordResetTokenRepository: PasswordResetTokenRepository = mockk(relaxed = true)
    private val mockUpdateUserService: UpdateUserService = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        passwordService = PasswordService(mockPasswordResetTokenRepository)
        clearAllMocks()
    }

    @Test
    @DisplayName("Deve codificar senha corretamente")
    fun `should encode password correctly`() {
        // Given
        val rawPassword = "mySecretPassword"

        // When
        val result = passwordService.encode(rawPassword)

        // Then
        assertAll(
            { assertNotNull(result, "Senha codificada não deve ser nula") },
            { assertTrue(result.isNotEmpty(), "Senha codificada não deve ser vazia") },
            { assertTrue(result.startsWith("$2a$"), "Senha deve usar BCrypt com prefixo correto") }
        )
    }

    @Test
    @DisplayName("Deve validar senha corretamente quando senhas coincidem")
    fun `should validate password correctly when passwords match`() {
        // Given
        val rawPassword = "mySecretPassword"
        val encodedPassword = passwordService.encode(rawPassword)

        // When
        val result = passwordService.matches(rawPassword, encodedPassword)

        // Then
        assertTrue(result, "Senhas iguais devem retornar true")
    }

    @Test
    @DisplayName("Deve validar senha corretamente quando senhas não coincidem")
    fun `should validate password correctly when passwords do not match`() {
        // Given
        val rawPassword = "mySecretPassword"
        val differentPassword = "differentPassword"
        val encodedPassword = passwordService.encode(differentPassword)

        // When
        val result = passwordService.matches(rawPassword, encodedPassword)

        // Then
        assertFalse(result, "Senhas diferentes devem retornar false")
    }

    @Test
    @DisplayName("Deve gerar salt aleatório corretamente")
    fun `should generate random salt correctly`() {
        // When
        val result = passwordService.saltPassword()

        // Then
        assertAll(
            { assertNotNull(result, "Salt não deve ser nulo") },
            { assertEquals(32, result.length, "Salt em hexadecimal deve ter 32 caracteres") },
            { assertTrue(result.matches(Regex("^[0-9a-f]+$")), "Salt deve conter apenas caracteres hexadecimais") }
        )
    }

    @Test
    @DisplayName("Deve gerar salts únicos em múltiplas chamadas")
    fun `should generate unique salts on multiple calls`() {
        // When
        val salt1 = passwordService.saltPassword()
        val salt2 = passwordService.saltPassword()
        val salt3 = passwordService.saltPassword()

        // Then
        assertAll(
            { assertNotNull(salt1, "Primeiro salt não deve ser nulo") },
            { assertNotNull(salt2, "Segundo salt não deve ser nulo") },
            { assertNotNull(salt3, "Terceiro salt não deve ser nulo") },
            { assertNotEquals(salt1, salt2, "Salt1 e Salt2 devem ser diferentes") },
            { assertNotEquals(salt1, salt3, "Salt1 e Salt3 devem ser diferentes") },
            { assertNotEquals(salt2, salt3, "Salt2 e Salt3 devem ser diferentes") },
            { assertEquals(32, salt1.length, "Primeiro salt deve ter 32 caracteres") },
            { assertEquals(32, salt2.length, "Segundo salt deve ter 32 caracteres") },
            { assertEquals(32, salt3.length, "Terceiro salt deve ter 32 caracteres") }
        )
    }

    @Test
    @DisplayName("Deve tratar senhas vazias na codificação")
    fun `should handle empty passwords in encoding`() {
        // Given
        val emptyPassword = ""

        // When
        val result = passwordService.encode(emptyPassword)

        // Then
        assertAll(
            { assertNotNull(result, "Resultado não deve ser nulo") },
            { assertTrue(result.isNotEmpty(), "Senha codificada não deve ser vazia mesmo para senha vazia") },
            { assertTrue(result.startsWith("$2a$"), "Deve usar BCrypt mesmo para senha vazia") }
        )
    }

    @Test
    @DisplayName("Deve tratar senhas vazias na validação")
    fun `should handle empty passwords in validation`() {
        // Given
        val emptyPassword = ""
        val encodedEmptyPassword = passwordService.encode(emptyPassword)

        // When
        val result = passwordService.matches(emptyPassword, encodedEmptyPassword)

        // Then
        assertTrue(result, "Senha vazia deve coincidir com sua própria codificação")
    }

    @Test
    @DisplayName("Deve codificar a mesma senha de forma diferente em cada chamada")
    fun `should encode same password differently each time`() {
        // Given
        val password = "samePassword"

        // When
        val encoded1 = passwordService.encode(password)
        val encoded2 = passwordService.encode(password)

        // Then
        assertAll(
            { assertNotNull(encoded1) },
            { assertNotNull(encoded2) },
            { assertNotEquals(encoded1, encoded2, "BCrypt deve gerar hashes diferentes para a mesma senha") },
            { assertTrue(passwordService.matches(password, encoded1), "Primeira codificação deve validar corretamente") },
            { assertTrue(passwordService.matches(password, encoded2), "Segunda codificação deve validar corretamente") }
        )
    }

    @Test
    @DisplayName("Deve rejeitar senha incorreta")
    fun `should reject incorrect password`() {
        // Given
        val correctPassword = "correctPassword"
        val incorrectPassword = "incorrectPassword"
        val encodedPassword = passwordService.encode(correctPassword)

        // When
        val result = passwordService.matches(incorrectPassword, encodedPassword)

        // Then
        assertFalse(result, "Senha incorreta deve retornar false")
    }

    @Test
    @DisplayName("Deve criar token de reset de senha para usuário")
    fun `should create password reset token for user`() {
        // Given
        val user = createTestUser()
        val token = "test-reset-token-123"
        val savedTokenSlot = slot<PasswordResetToken>()

        every { mockPasswordResetTokenRepository.save(capture(savedTokenSlot)) } returns PasswordResetToken()

        // When
        val result = passwordService.createPasswordResetTokenForUser(user, token)

        // Then
        assertEquals(token, result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.save(any()) }

        val capturedToken = savedTokenSlot.captured
        assertEquals(token, capturedToken.token)
        assertEquals(user, capturedToken.user)
        assertNotNull(capturedToken.expiryDate)
    }

    @Test
    @DisplayName("Deve validar token de reset de senha válido")
    fun `should validate valid password reset token`() {
        // Given
        val validToken = "valid-token-123"
        val futureDate = Date(System.currentTimeMillis() + 60000) // 1 minute in future
        val passwordResetToken = PasswordResetToken(
            token = validToken,
            user = createTestUser(),
            expiryDate = futureDate
        )

        every { mockPasswordResetTokenRepository.findByToken(validToken) } returns Optional.of(passwordResetToken)

        // When
        val result = passwordService.validatePasswordResetToken(validToken)

        // Then
        assertTrue(result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(validToken) }
    }

    @Test
    @DisplayName("Deve retornar false para token de reset de senha inválido")
    fun `should return false for invalid password reset token`() {
        // Given
        val invalidToken = "invalid-token-123"

        every { mockPasswordResetTokenRepository.findByToken(invalidToken) } returns Optional.empty()

        // When
        val result = passwordService.validatePasswordResetToken(invalidToken)

        // Then
        assertFalse(result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(invalidToken) }
    }

    @Test
    @DisplayName("Deve retornar false para token de reset de senha expirado")
    fun `should return false for expired password reset token`() {
        // Given
        val expiredToken = "expired-token-123"
        val pastDate = Date(System.currentTimeMillis() - 60000) // 1 minute in past
        val passwordResetToken = PasswordResetToken(
            token = expiredToken,
            user = createTestUser(),
            expiryDate = pastDate
        )

        every { mockPasswordResetTokenRepository.findByToken(expiredToken) } returns Optional.of(passwordResetToken)

        // When
        val result = passwordService.validatePasswordResetToken(expiredToken)

        // Then
        assertFalse(result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(expiredToken) }
    }

    @Test
    @DisplayName("Deve salvar nova senha com token válido")
    fun `should save new password with valid token`() {
        // Given
        val validToken = "valid-token-456"
        val newPassword = "newSecurePassword123"
        val user = createTestUser()
        val locale = Locale.ENGLISH

        val updateRequest = UpdateUserRequest(
            username = "testuser",
            email = "test@example.com",
            oldPassword = "oldPassword",
            newPassword = newPassword,
            token = validToken
        )

        val futureDate = Date(System.currentTimeMillis() + 60000)
        val passwordResetToken = PasswordResetToken(
            token = validToken,
            user = user,
            expiryDate = futureDate
        )

        every { mockPasswordResetTokenRepository.findByToken(validToken) } returns Optional.of(passwordResetToken)
        every { mockUpdateUserService.saveUser(any()) } returns mockk()

        // When
        val result = passwordService.savePassword(locale, updateRequest)

        // Then
        assertEquals("auth.message.resetPasswordSuc", result.message)
        verify(exactly = 2) { mockPasswordResetTokenRepository.findByToken(validToken) } // Called twice: once in validatePasswordResetToken, once in savePassword
        verify(exactly = 1) { mockUpdateUserService.saveUser(any()) }
    }

    @Test
    @DisplayName("Deve retornar erro para token inválido ao salvar senha")
    fun `should return error for invalid token when saving password`() {
        // Given
        val invalidToken = "invalid-token-789"
        val locale = Locale.ENGLISH

        val updateRequest = UpdateUserRequest(
            username = "testuser",
            email = "test@example.com",
            oldPassword = "oldPassword",
            newPassword = "newPassword",
            token = invalidToken
        )

        every { mockPasswordResetTokenRepository.findByToken(invalidToken) } returns Optional.empty()

        // When
        val result = passwordService.savePassword(locale, updateRequest)

        // Then
        assertEquals("auth.message.invalid", result.message)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(invalidToken) }
        verify(exactly = 0) { mockUpdateUserService.saveUser(any()) }
    }

    @Test
    @DisplayName("Deve encontrar token válido e não expirado")
    fun `should find valid and non-expired token`() {
        // Given
        val validToken = "valid-non-expired-token"
        val futureDate = Date(System.currentTimeMillis() + 60000)
        val passwordResetToken = PasswordResetToken(
            token = validToken,
            user = createTestUser(),
            expiryDate = futureDate
        )

        every { mockPasswordResetTokenRepository.findByToken(validToken) } returns Optional.of(passwordResetToken)

        // When
        val result = passwordService.isTokenFound(validToken)

        // Then
        assertTrue(result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(validToken) }
    }

    @Test
    @DisplayName("Deve não encontrar token inexistente")
    fun `should not find non-existent token`() {
        // Given
        val nonExistentToken = "non-existent-token"

        every { mockPasswordResetTokenRepository.findByToken(nonExistentToken) } returns Optional.empty()

        // When
        val result = passwordService.isTokenFound(nonExistentToken)

        // Then
        assertFalse(result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(nonExistentToken) }
    }

    @Test
    @DisplayName("Deve não encontrar token expirado")
    fun `should not find expired token`() {
        // Given
        val expiredToken = "expired-token"
        val pastDate = Date(System.currentTimeMillis() - 60000)
        val passwordResetToken = PasswordResetToken(
            token = expiredToken,
            user = createTestUser(),
            expiryDate = pastDate
        )

        every { mockPasswordResetTokenRepository.findByToken(expiredToken) } returns Optional.of(passwordResetToken)

        // When
        val result = passwordService.isTokenFound(expiredToken)

        // Then
        assertFalse(result)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(expiredToken) }
    }

    @Test
    @DisplayName("Deve identificar token expirado corretamente")
    fun `should identify expired token correctly`() {
        // Given
        val pastDate = Date(System.currentTimeMillis() - 60000) // 1 minute ago
        val expiredToken = PasswordResetToken(
            token = "test-token",
            user = createTestUser(),
            expiryDate = pastDate
        )

        // When
        val result = passwordService.isTokenExpired(expiredToken)

        // Then
        assertTrue(result, "Token com data no passado deve ser considerado expirado")
    }

    @Test
    @DisplayName("Deve identificar token não expirado corretamente")
    fun `should identify non-expired token correctly`() {
        // Given
        val futureDate = Date(System.currentTimeMillis() + 60000) // 1 minute in future
        val validToken = PasswordResetToken(
            token = "test-token",
            user = createTestUser(),
            expiryDate = futureDate
        )

        // When
        val result = passwordService.isTokenExpired(validToken)

        // Then
        assertFalse(result, "Token com data no futuro não deve ser considerado expirado")
    }

    @Test
    @DisplayName("Deve tratar token nulo ou vazio")
    fun `should handle null or empty token`() {
        // Given
        val locale = Locale.FRENCH

        val updateRequestWithNullToken = UpdateUserRequest(
            username = "testuser",
            email = "test@example.com",
            oldPassword = "oldPassword",
            newPassword = "newPassword",
            token = null
        )

        val updateRequestWithEmptyToken = UpdateUserRequest(
            username = "testuser",
            email = "test@example.com",
            oldPassword = "oldPassword",
            newPassword = "newPassword",
            token = ""
        )

        every { mockPasswordResetTokenRepository.findByToken("") } returns Optional.empty()

        // When
        val resultNull = passwordService.savePassword(locale, updateRequestWithNullToken)
        val resultEmpty = passwordService.savePassword(locale, updateRequestWithEmptyToken)

        // Then
        assertEquals("auth.message.invalid", resultNull.message)
        assertEquals("auth.message.invalid", resultEmpty.message)
    }

    @Test
    @DisplayName("Deve processar token expirado corretamente")
    fun `should process expired token correctly`() {
        // Given
        val expiredToken = "expired-token-789"
        val locale = Locale.of("pt", "BR")

        val updateRequest = UpdateUserRequest(
            username = "testuser",
            email = "test@example.com",
            oldPassword = "oldPassword",
            newPassword = "newPassword",
            token = expiredToken
        )

        val pastDate = Date(System.currentTimeMillis() - 60000)
        val passwordResetToken = PasswordResetToken(
            token = expiredToken,
            user = createTestUser(),
            expiryDate = pastDate
        )

        every { mockPasswordResetTokenRepository.findByToken(expiredToken) } returns Optional.of(passwordResetToken)

        // When
        val result = passwordService.savePassword(locale, updateRequest)

        // Then
        assertEquals("auth.message.invalid", result.message)
        verify(exactly = 1) { mockPasswordResetTokenRepository.findByToken(expiredToken) }
        verify(exactly = 0) { mockUpdateUserService.saveUser(any()) }
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
