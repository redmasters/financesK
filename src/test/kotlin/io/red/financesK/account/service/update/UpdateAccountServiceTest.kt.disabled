package io.red.financesK.account.service.update

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.red.financesK.account.controller.request.UpdateAccountRequest
import io.red.financesK.account.model.Account
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.global.exception.NotFoundException
import io.red.financesK.user.model.AppUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class UpdateAccountServiceTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var updateAccountService: UpdateAccountService

    @BeforeEach
    fun setUp() {
        accountRepository = mockk()
        updateAccountService = UpdateAccountService(accountRepository)
    }

    @Test
    @DisplayName("Deve atualizar conta com sucesso quando todos os campos são fornecidos")
    fun `should update account successfully when all fields are provided`() {
        // Given
        val accountId = 1
        val user = AppUser(id = 1, username = "testuser", email = "test@test.com", passwordHash = "123456")
        val existingAccount = Account(
            accountId = accountId,
            accountName = "Old Account",
            accountDescription = "Old Description",
            accountCurrentBalance = 10000, // 100.00 in cents
            accountCurrency = "BRL",
            userId = user,
            createdAt = Instant.now()
        )

        val request = UpdateAccountRequest(
            accountName = "New Account",
            accountDescription = "New Description",
            accountCurrentBalance = BigDecimal("200.00"),
            accountCurrency = "USD"
        )

        val updatedAccount = existingAccount.copy(
            accountName = "New Account",
            accountDescription = "New Description",
            accountCurrentBalance = 20000, // 200.00 in cents
            accountCurrency = "USD",
            updatedAt = Instant.now()
        )

        every { accountRepository.findById(accountId) } returns Optional.of(existingAccount)
        every { accountRepository.save(any<Account>()) } returns updatedAccount

        // When
        val result = updateAccountService.execute(accountId, request)

        // Then
        assertNotNull(result)
        assertEquals(accountId, result.accountId)
        assertEquals("Account updated successfully", result.message)
        assertNotNull(result.updatedAt)

        verify { accountRepository.findById(accountId) }
        verify { accountRepository.save(any<Account>()) }
    }

    @Test
    @DisplayName("Deve atualizar apenas campos fornecidos (atualização parcial)")
    fun `should update only provided fields - partial update`() {
        // Given
        val accountId = 2
        val user = AppUser(id = 1, username = "testuser", email = "test@test.com", passwordHash = "123456")
        val existingAccount = Account(
            accountId = accountId,
            accountName = "Original Account",
            accountDescription = "Original Description",
            accountCurrentBalance = 5000, // 50.00 in cents
            accountCurrency = "BRL",
            userId = user,
            createdAt = Instant.now()
        )

        val request = UpdateAccountRequest(
            accountName = "Updated Name Only",
            accountDescription = null,
            accountCurrentBalance = null,
            accountCurrency = null
        )

        val updatedAccount = existingAccount.copy(
            accountName = "Updated Name Only",
            updatedAt = Instant.now()
        )

        every { accountRepository.findById(accountId) } returns Optional.of(existingAccount)
        every { accountRepository.save(any<Account>()) } returns updatedAccount

        // When
        val result = updateAccountService.execute(accountId, request)

        // Then
        assertNotNull(result)
        assertEquals(accountId, result.accountId)
        assertNotNull(result.updatedAt)

        verify { accountRepository.findById(accountId) }
        verify { accountRepository.save(any<Account>()) }
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando conta não existe")
    fun `should throw NotFoundException when account does not exist`() {
        // Given
        val accountId = 999
        val request = UpdateAccountRequest(
            accountName = "New Name"
        )

        every { accountRepository.findById(accountId) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<NotFoundException> {
            updateAccountService.execute(accountId, request)
        }

        assertEquals("Account not found with id: $accountId", exception.message)
        verify { accountRepository.findById(accountId) }
        verify(exactly = 0) { accountRepository.save(any<Account>()) }
    }

    @Test
    @DisplayName("Deve atualizar conta com saldo zero (boundary)")
    fun `should update account with zero balance - boundary`() {
        // Given
        val accountId = 3
        val user = AppUser(id = 1, username = "testuser", email = "test@test.com", passwordHash = "123456")
        val existingAccount = Account(
            accountId = accountId,
            accountName = "Test Account",
            accountDescription = "Test Description",
            accountCurrentBalance = 10000,
            accountCurrency = "BRL",
            userId = user,
            createdAt = Instant.now()
        )

        val request = UpdateAccountRequest(
            accountCurrentBalance = BigDecimal.ZERO
        )

        val updatedAccount = existingAccount.copy(
            accountCurrentBalance = 0,
            updatedAt = Instant.now()
        )

        every { accountRepository.findById(accountId) } returns Optional.of(existingAccount)
        every { accountRepository.save(any<Account>()) } returns updatedAccount

        // When
        val result = updateAccountService.execute(accountId, request)

        // Then
        assertNotNull(result)
        assertEquals(accountId, result.accountId)
        assertNotNull(result.updatedAt)

        verify { accountRepository.findById(accountId) }
        verify { accountRepository.save(any<Account>()) }
    }

    @Test
    @DisplayName("Deve atualizar conta com saldo negativo (boundary)")
    fun `should update account with negative balance - boundary`() {
        // Given
        val accountId = 4
        val user = AppUser(id = 1, username = "testuser", email = "test@test.com", passwordHash = "123456")
        val existingAccount = Account(
            accountId = accountId,
            accountName = "Test Account",
            accountDescription = "Test Description",
            accountCurrentBalance = 10000,
            accountCurrency = "BRL",
            userId = user,
            createdAt = Instant.now()
        )

        val request = UpdateAccountRequest(
            accountCurrentBalance = BigDecimal("-50.00")
        )

        val updatedAccount = existingAccount.copy(
            accountCurrentBalance = -5000, // -50.00 in cents
            updatedAt = Instant.now()
        )

        every { accountRepository.findById(accountId) } returns Optional.of(existingAccount)
        every { accountRepository.save(any<Account>()) } returns updatedAccount

        // When
        val result = updateAccountService.execute(accountId, request)

        // Then
        assertNotNull(result)
        assertEquals(accountId, result.accountId)
        assertNotNull(result.updatedAt)

        verify { accountRepository.findById(accountId) }
        verify { accountRepository.save(any<Account>()) }
    }

    @Test
    @DisplayName("Deve atualizar conta sem fornecer nenhum campo (request vazio)")
    fun `should handle empty update request - no fields provided`() {
        // Given
        val accountId = 5
        val user = AppUser(id = 1, username = "testuser", email = "test@test.com", passwordHash = "123456")
        val existingAccount = Account(
            accountId = accountId,
            accountName = "Unchanged Account",
            accountDescription = "Unchanged Description",
            accountCurrentBalance = 15000,
            accountCurrency = "BRL",
            userId = user,
            createdAt = Instant.now()
        )

        val request = UpdateAccountRequest()

        val updatedAccount = existingAccount.copy(
            updatedAt = Instant.now()
        )

        every { accountRepository.findById(accountId) } returns Optional.of(existingAccount)
        every { accountRepository.save(any<Account>()) } returns updatedAccount

        // When
        val result = updateAccountService.execute(accountId, request)

        // Then
        assertNotNull(result)
        assertEquals(accountId, result.accountId)
        assertNotNull(result.updatedAt)

        verify { accountRepository.findById(accountId) }
        verify { accountRepository.save(any<Account>()) }
    }

    @Test
    @DisplayName("Deve atualizar conta com valores máximos permitidos (boundary)")
    fun `should update account with maximum allowed values - boundary`() {
        // Given
        val accountId = 6
        val user = AppUser(id = 1, username = "testuser", email = "test@test.com", passwordHash = "123456")
        val existingAccount = Account(
            accountId = accountId,
            accountName = "Test Account",
            accountDescription = "Test Description",
            accountCurrentBalance = 0,
            accountCurrency = "BRL",
            userId = user,
            createdAt = Instant.now()
        )

        val maxName = "A".repeat(100)
        val maxDescription = "B".repeat(255)
        val maxBalance = BigDecimal("999999999.99")

        val request = UpdateAccountRequest(
            accountName = maxName,
            accountDescription = maxDescription,
            accountCurrentBalance = maxBalance,
            accountCurrency = "EUR"
        )

        val updatedAccount = existingAccount.copy(
            accountName = maxName,
            accountDescription = maxDescription,
            accountCurrentBalance = 99999999999.toInt(), // max balance in cents as Int
            accountCurrency = "EUR",
            updatedAt = Instant.now()
        )

        every { accountRepository.findById(accountId) } returns Optional.of(existingAccount)
        every { accountRepository.save(any<Account>()) } returns updatedAccount

        // When
        val result = updateAccountService.execute(accountId, request)

        // Then
        assertNotNull(result)
        assertEquals(accountId, result.accountId)
        assertNotNull(result.updatedAt)

        verify { accountRepository.findById(accountId) }
        verify { accountRepository.save(any<Account>()) }
    }
}
