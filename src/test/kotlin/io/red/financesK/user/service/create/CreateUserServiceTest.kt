package io.red.financesK.user.service.create

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.user.controller.request.CreateUserRequest
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

class CreateUserServiceTest {
    private val appUserRepository = mockk<AppUserRepository>(relaxed = true)
    private val service = CreateUserService(appUserRepository)

    @Test
    @DisplayName("Deve criar usuário com valores normais")
    fun `should create user with normal values`() {
        val request = CreateUserRequest(
            username = "normaluser",
            email = "normal@example.com",
            passwordHash = "hash123"
        )
        val user = AppUser(
            id = 1,
            username = request.username,
            email = request.email,
            passwordHash = request.passwordHash,
            createdAt = Instant.now()
        )
        every { appUserRepository.save(any()) } returns user
        val response = service.execute(request)
        assertEquals(user.id, response.id)
        assertEquals(user.username, response.username)
        assertEquals(user.email, response.email)
        assertEquals(user.createdAt, response.createdAt)
        verify(exactly = 1) { appUserRepository.save(any()) }
    }

    @Test
    @DisplayName("Deve lançar exceção se username tiver menos de 6 caracteres")
    fun `should throw exception for username less than 6 chars`() {
        val request = CreateUserRequest(
            username = "abc",
            email = "short@example.com",
            passwordHash = "validpass"
        )
        val exception = assertThrows(ValidationException::class.java) {
            service.execute(request)
        }
        assertEquals("Username must be at least 6 characters long.", exception.message)
    }

    @Test
    @DisplayName("Deve lançar exceção se senha tiver menos de 6 caracteres")
    fun `should throw exception for password less than 6 chars`() {
        val request = CreateUserRequest(
            username = "validuser",
            email = "shortpass@example.com",
            passwordHash = "123"
        )
        val exception = assertThrows(ValidationException::class.java) {
            service.execute(request)
        }
        assertEquals("Password must be at least 6 characters long.", exception.message)
    }

    @Test
    @DisplayName("Deve criar usuário com tamanho máximo de username")
    fun `should create user with maximum username length`() {
        val maxUsername = "a".repeat(50)
        val request = CreateUserRequest(
            username = maxUsername,
            email = "max@example.com",
            passwordHash = "hashmax"
        )
        val user = AppUser(
            id = 3,
            username = request.username,
            email = request.email,
            passwordHash = request.passwordHash,
            createdAt = Instant.now()
        )
        every { appUserRepository.save(any()) } returns user
        val response = service.execute(request)
        assertEquals(user.username, response.username)
        verify(exactly = 1) { appUserRepository.save(any()) }
    }

    @Test
    @DisplayName("Deve criar usuário com tamanho mínimo de email")
    fun `should create user with minimum email length`() {
        val request = CreateUserRequest(
            username = "minemail",
            email = "a@b.c",
            passwordHash = "hashminemail"
        )
        val user = AppUser(
            id = 4,
            username = request.username,
            email = request.email,
            passwordHash = request.passwordHash,
            createdAt = Instant.now()
        )
        every { appUserRepository.save(any()) } returns user
        val response = service.execute(request)
        assertEquals(user.email, response.email)
        verify(exactly = 1) { appUserRepository.save(any()) }
    }

    @Test
    @DisplayName("Deve criar usuário com tamanho máximo de email")
    fun `should create user with maximum email length`() {
        val maxEmail = "a".repeat(90) + "@example.com"
        val request = CreateUserRequest(
            username = "maxemail",
            email = maxEmail,
            passwordHash = "hashmaxemail"
        )
        val user = AppUser(
            id = 5,
            username = request.username,
            email = request.email,
            passwordHash = request.passwordHash,
            createdAt = Instant.now()
        )
        every { appUserRepository.save(any()) } returns user
        val response = service.execute(request)
        assertEquals(user.email, response.email)
        verify(exactly = 1) { appUserRepository.save(any()) }
    }
}
