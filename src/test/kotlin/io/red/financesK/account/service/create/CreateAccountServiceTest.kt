package io.red.financesK.account.service.create

import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.account.balance.service.history.CreateBalanceHistory
import io.red.financesK.account.controller.request.CreateAccountRequest
import io.red.financesK.account.model.Account
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
class CreateAccountServiceTest {

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var appUserRepository: AppUserRepository

    @Mock
    private lateinit var createBalanceHistory: CreateBalanceHistory

    @InjectMocks
    private lateinit var service: CreateAccountService

    @Captor
    private lateinit var accountCaptor: ArgumentCaptor<Account>

    private lateinit var user: AppUser

    @BeforeEach
    fun setup() {
        user = AppUser(1, "testUser", "test@user.com", "hash", Instant.now())
    }

    @Test
    @DisplayName("Deve criar uma conta com sucesso e registrar o histórico de saldo")
    fun `should create account successfully and log balance history`() {
        // Given
        val request = CreateAccountRequest(
            name = "Conta Corrente",
            description = "Conta principal do usuário",
            balance = "1000.00",
            currency = "BRL",
            userId = 1
        )

        val savedAccount = Account(
            accountId = 1,
            accountName = request.name,
            accountDescription = request.description,
            accountInitialBalance = BigDecimal("1000.00"),
            accountCurrency = request.currency,
            userId = user
        )

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(savedAccount)

        // When
        val response = service.createAccount(request)

        // Then
        assertNotNull(response)
        assertEquals(savedAccount.accountId, response.accountId)
        assertEquals(savedAccount.accountName, response.name)
        assertEquals("1000.00", response.balance)

        verify(appUserRepository).findById(1)
        verify(accountRepository).save(accountCaptor.capture())
        val capturedAccount = accountCaptor.value
        assertEquals("Conta Corrente", capturedAccount.accountName)
        assertEquals(BigDecimal("1000.00"), capturedAccount.accountInitialBalance)

        verify(createBalanceHistory).createBalanceHistory(
            accountId = 1,
            amount = BigDecimal("1000.00"),
            operationType = AccountOperationType.INITIAL_BALANCE
        )
    }

    @Test
    @DisplayName("Deve falhar ao tentar criar conta para um usuário inexistente")
    fun `should fail when creating account for non-existent user`() {
        // Given
        val request = CreateAccountRequest("Conta Fantasma", "desc", "100.0", "BRL", 999)
        `when`(appUserRepository.findById(999)).thenReturn(Optional.empty())

        // When & Then
        val exception = assertThrows<ValidationException> {
            service.createAccount(request)
        }
        assertEquals("User with id 999 not found", exception.message)
        verify(appUserRepository).findById(999)
        verifyNoInteractions(accountRepository)
        verifyNoInteractions(createBalanceHistory)
    }

    @Test
    @DisplayName("Deve falhar se o nome da conta estiver em branco")
    fun `should fail if account name is blank`() {
        // Given
        val request = CreateAccountRequest(" ", "desc", "100.0", "BRL", 1)

        // When & Then
        val exception = assertThrows<ValidationException> {
            service.createAccount(request)
        }
        assertEquals("Account name cannot be blank", exception.message)
        verifyNoInteractions(appUserRepository)
    }

    @Test
    @DisplayName("Deve falhar se o código da moeda for inválido")
    fun `should fail if currency code is invalid`() {
        // Given
        val request = CreateAccountRequest("Conta Válida", "desc", "100.0", "INVALID", 1)

        // When & Then
        val exception = assertThrows<ValidationException> {
            service.createAccount(request)
        }
        assertEquals("Currency must be a 3-letter code (e.g., BRL, USD)", exception.message)
        verifyNoInteractions(appUserRepository)
    }

    @Test
    @DisplayName("Deve criar conta com saldo zero se o valor for nulo ou em branco")
    fun `should create account with zero balance if value is null or blank`() {
        // Given
        val request = CreateAccountRequest("Conta sem Saldo", "desc", null, "BRL", 1)
        val savedAccount = Account(1, request.name, request.description, BigDecimal.ZERO, request.currency, user)

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(savedAccount)

        // When
        service.createAccount(request)

        // Then
        verify(accountRepository).save(accountCaptor.capture())
        assertEquals(BigDecimal.ZERO, accountCaptor.value.accountInitialBalance)
        verify(createBalanceHistory).createBalanceHistory(1, BigDecimal.ZERO, AccountOperationType.INITIAL_BALANCE)
    }

    @Test
    @DisplayName("Deve falhar se o saldo inicial for negativo")
    fun `should fail if initial balance is negative`() {
        // Given
        val request = CreateAccountRequest("Conta Negativa", "desc", "-100.00", "BRL", 1)
        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))

        // When & Then
        val exception = assertThrows<ValidationException> {
            service.createAccount(request)
        }
        assertEquals("Initial balance cannot be negative", exception.message)
        verify(appUserRepository).findById(1)
        verifyNoInteractions(accountRepository)
    }

    @Test
    @DisplayName("Deve continuar a criação da conta mesmo se o histórico de saldo falhar")
    fun `should create account even if balance history creation fails`() {
        // Given
        val request = CreateAccountRequest("Conta Resiliente", "desc", "200.00", "BRL", 1)
        val savedAccount = Account(1, request.name, request.description, BigDecimal("200.00"), request.currency, user)

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(savedAccount)
        doThrow(RuntimeException("Falha ao salvar histórico")).`when`(createBalanceHistory).createBalanceHistory(anyInt(), any(), any())

        // When & Then
        assertDoesNotThrow {
            service.createAccount(request)
        }
        verify(accountRepository).save(any(Account::class.java))
        verify(createBalanceHistory).createBalanceHistory(anyInt(), any(), any())
    }
}
