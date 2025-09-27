package io.red.financesK.auth.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PasswordServiceTest {

    private lateinit var passwordService: PasswordService

    @BeforeEach
    fun setUp() {
        passwordService = PasswordService()
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
}
