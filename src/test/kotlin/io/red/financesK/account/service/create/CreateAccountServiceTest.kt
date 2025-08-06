package io.red.financesK.account.service.create

import io.red.financesK.account.controller.request.CreateAccountRequest
import io.red.financesK.account.model.Account
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
class CreateAccountServiceTest {

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var appUserRepository: AppUserRepository

    @InjectMocks
    private lateinit var createAccountService: CreateAccountService

    private lateinit var user: AppUser
    private lateinit var account: Account

    @BeforeEach
    fun setup() {
        user = AppUser(
            id = 1,
            username = "testUser",
            email = "test@test.com",
            passwordHash = "hashedPassword",
            createdAt = Instant.now()
        )

        account = Account(
            accountId = 1,
            accountName = "Conta Corrente",
            accountDescription = "Descrição da conta corrente",
            accountInitialBalance = BigDecimal("1000.00").setScale(2, RoundingMode.HALF_EVEN),
            accountCurrency = "BRL",
            userId = user
        )
    }

    @Test
    @DisplayName("Deve criar uma conta com sucesso")
    fun `should create account successfully`() {
        val request = CreateAccountRequest(
            name = "Conta Corrente",
            balance = "1000.00",
            userId = 1
        )

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(account)

        assertDoesNotThrow {
            createAccountService.createAccount(request)
        }

        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve criar conta com saldo inicial nulo")
    fun `should create account with null initial balance`() {
        val request = CreateAccountRequest(
            name = "Conta Sem Saldo",
            balance = null,
            userId = 1
        )

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(account.copy(accountInitialBalance = null))

        assertDoesNotThrow {
            createAccountService.createAccount(request)
        }

        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve criar conta com saldo inicial zerado")
    fun `should create account with zero initial balance`() {
        val request = CreateAccountRequest(
            name = "Conta Zerada",
            balance = "0.00",
            userId = 1
        )

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(
            account.copy(accountInitialBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
        )

        assertDoesNotThrow {
            createAccountService.createAccount(request)
        }

        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado")
    fun `should throw exception when user not found`() {
        val request = CreateAccountRequest(
            name = "Conta Teste",
            balance = "500.00",
            userId = 999
        )

        `when`(appUserRepository.findById(999)).thenReturn(Optional.empty())

        val exception = assertThrows(IllegalArgumentException::class.java) {
            createAccountService.createAccount(request)
        }

        assertEquals("User with id 999 not found", exception.message)
        verify(appUserRepository).findById(999)
        verify(accountRepository, never()).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve tratar valor inválido de saldo inicial")
    fun `should handle invalid initial balance value`() {
        val request = CreateAccountRequest(
            name = "Conta Inválida",
            balance = "valor_inválido",
            userId = 1
        )

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(
            account.copy(accountInitialBalance = null)
        )

        assertDoesNotThrow {
            createAccountService.createAccount(request)
        }

        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve criar conta com valor decimal formatado corretamente")
    fun `should create account with properly formatted decimal value`() {
        val request = CreateAccountRequest(
            name = "Conta Decimal",
            balance = "1234.567", // Será arredondado para 2 casas decimais
            userId = 1
        )

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(
            account.copy(accountInitialBalance = BigDecimal("1234.57").setScale(2, RoundingMode.HALF_EVEN))
        )

        assertDoesNotThrow {
            createAccountService.createAccount(request)
        }

        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve criar conta com nome longo (boundary test)")
    fun `should create account with long name`() {
        val longName = "A".repeat(100) // Nome de 100 caracteres (limite da coluna)
        val request = CreateAccountRequest(
            name = longName,
            balance = "1000.00",
            userId = 1
        )

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(
            account.copy(accountName = longName)
        )

        assertDoesNotThrow {
            createAccountService.createAccount(request)
        }

        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve criar conta com valor monetário grande (edge test)")
    fun `should create account with large monetary value`() {
        val request = CreateAccountRequest(
            name = "Conta Grande",
            balance = "99999999.99", // Valor máximo para precision 10, scale 2
            userId = 1
        )

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(
            account.copy(accountInitialBalance = BigDecimal("99999999.99").setScale(2, RoundingMode.HALF_EVEN))
        )

        assertDoesNotThrow {
            createAccountService.createAccount(request)
        }

        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve criar conta com valor negativo")
    fun `should create account with negative balance`() {
        val request = CreateAccountRequest(
            name = "Conta Negativa",
            balance = "-500.00",
            userId = 1
        )

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(
            account.copy(accountInitialBalance = BigDecimal("-500.00").setScale(2, RoundingMode.HALF_EVEN))
        )

        assertDoesNotThrow {
            createAccountService.createAccount(request)
        }

        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve criar conta com descrição personalizada")
    fun `should create account with custom description`() {
        val request = CreateAccountRequest(
            name = "Conta Poupança",
            description = "Conta para guardar dinheiro",
            balance = "1000.00",
            userId = 1
        )

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(
            account.copy(accountName = "Conta Poupança", accountDescription = "Conta para guardar dinheiro")
        )

        assertDoesNotThrow {
            createAccountService.createAccount(request)
        }

        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve criar conta com moeda diferente")
    fun `should create account with different currency`() {
        val request = CreateAccountRequest(
            name = "Conta USD",
            balance = "1000.00",
            currency = "USD",
            userId = 1
        )

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(
            account.copy(accountName = "Conta USD", accountCurrency = "USD")
        )

        assertDoesNotThrow {
            createAccountService.createAccount(request)
        }

        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve criar conta com descrição longa (boundary test)")
    fun `should create account with long description`() {
        val longDescription = "D".repeat(255) // Descrição de 255 caracteres (limite da coluna)
        val request = CreateAccountRequest(
            name = "Conta com Descrição Longa",
            description = longDescription,
            balance = "500.00",
            userId = 1
        )

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(
            account.copy(accountName = "Conta com Descrição Longa", accountDescription = longDescription)
        )

        assertDoesNotThrow {
            createAccountService.createAccount(request)
        }

        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }
}
