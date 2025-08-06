package io.red.financesK.account.service.update

import io.red.financesK.account.controller.request.UpdateAccountRequest
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
class UpdateAccountServiceTest {

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var appUserRepository: AppUserRepository

    @InjectMocks
    private lateinit var updateAccountService: UpdateAccountService

    private lateinit var user: AppUser
    private lateinit var newUser: AppUser
    private lateinit var existingAccount: Account
    private lateinit var updatedAccount: Account

    @BeforeEach
    fun setup() {
        user = AppUser(
            id = 1,
            username = "testUser",
            email = "test@test.com",
            passwordHash = "hashedPassword",
            createdAt = Instant.now()
        )

        newUser = AppUser(
            id = 2,
            username = "newUser",
            email = "new@test.com",
            passwordHash = "hashedPassword",
            createdAt = Instant.now()
        )

        existingAccount = Account(
            accountId = 1,
            accountName = "Conta Original",
            accountDescription = "Descrição original",
            accountInitialBalance = BigDecimal("1000.00").setScale(2, RoundingMode.HALF_EVEN),
            accountCurrency = "BRL",
            userId = user
        )

        updatedAccount = Account(
            accountId = 1,
            accountName = "Conta Atualizada",
            accountDescription = "Descrição atualizada",
            accountInitialBalance = BigDecimal("2000.00").setScale(2, RoundingMode.HALF_EVEN),
            accountCurrency = "USD",
            userId = newUser
        )
    }

    @Test
    @DisplayName("Deve atualizar conta com sucesso")
    fun `should update account successfully`() {
        val request = UpdateAccountRequest(
            name = "Conta Atualizada",
            description = "Descrição atualizada",
            balance = "2000.00",
            currency = "USD",
            userId = 2
        )

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(2)).thenReturn(Optional.of(newUser))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(updatedAccount)

        val response = updateAccountService.updateAccount(1, request)

        assertNotNull(response)
        assertEquals(1, response.accountId)
        assertEquals("Conta Atualizada", response.name)
        assertEquals("Descrição atualizada", response.description)
        assertEquals("2000.00", response.balance)
        assertEquals("USD", response.currency)
        assertEquals(2, response.userId)

        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(2)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve atualizar conta com saldo inicial nulo")
    fun `should update account with null initial balance`() {
        val request = UpdateAccountRequest(
            name = "Conta Sem Saldo",
            balance = null,
            userId = 1
        )

        val accountWithNullBalance = existingAccount.copy()
        accountWithNullBalance.accountName = "Conta Sem Saldo"
        accountWithNullBalance.accountInitialBalance = null

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(accountWithNullBalance)

        val response = updateAccountService.updateAccount(1, request)

        assertNotNull(response)
        assertEquals("Conta Sem Saldo", response.name)
        assertNull(response.balance)

        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve atualizar conta com saldo inicial zerado")
    fun `should update account with zero initial balance`() {
        val request = UpdateAccountRequest(
            name = "Conta Zerada",
            balance = "0.00",
            userId = 1
        )

        val accountWithZeroBalance = existingAccount.copy()
        accountWithZeroBalance.accountName = "Conta Zerada"
        accountWithZeroBalance.accountInitialBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN)

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(accountWithZeroBalance)

        val response = updateAccountService.updateAccount(1, request)

        assertNotNull(response)
        assertEquals("Conta Zerada", response.name)
        assertEquals("0.00", response.balance)

        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve lançar exceção quando conta não for encontrada")
    fun `should throw exception when account not found`() {
        val request = UpdateAccountRequest(
            name = "Conta Teste",
            balance = "500.00",
            userId = 1
        )

        `when`(accountRepository.findById(999)).thenReturn(Optional.empty())

        val exception = assertThrows(IllegalArgumentException::class.java) {
            updateAccountService.updateAccount(999, request)
        }

        assertEquals("Account with id 999 not found", exception.message)
        verify(accountRepository).findById(999)
        verify(appUserRepository, never()).findById(any())
        verify(accountRepository, never()).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado")
    fun `should throw exception when user not found`() {
        val request = UpdateAccountRequest(
            name = "Conta Teste",
            balance = "500.00",
            userId = 999
        )

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(999)).thenReturn(Optional.empty())

        val exception = assertThrows(IllegalArgumentException::class.java) {
            updateAccountService.updateAccount(1, request)
        }

        assertEquals("User with id 999 not found", exception.message)
        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(999)
        verify(accountRepository, never()).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve tratar valor inválido de saldo inicial")
    fun `should handle invalid initial balance value`() {
        val request = UpdateAccountRequest(
            name = "Conta Inválida",
            balance = "valor_inválido",
            userId = 1
        )

        val accountWithNullBalance = existingAccount.copy()
        accountWithNullBalance.accountName = "Conta Inválida"
        accountWithNullBalance.accountInitialBalance = null

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(accountWithNullBalance)

        val response = updateAccountService.updateAccount(1, request)

        assertNotNull(response)
        assertEquals("Conta Inválida", response.name)
        assertNull(response.balance)

        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve atualizar conta com valor decimal formatado corretamente")
    fun `should update account with properly formatted decimal value`() {
        val request = UpdateAccountRequest(
            name = "Conta Decimal",
            balance = "1234.567", // Será arredondado para 2 casas decimais
            userId = 1
        )

        val accountWithFormattedBalance = existingAccount.copy()
        accountWithFormattedBalance.accountName = "Conta Decimal"
        accountWithFormattedBalance.accountInitialBalance = BigDecimal("1234.57").setScale(2, RoundingMode.HALF_EVEN)

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(accountWithFormattedBalance)

        val response = updateAccountService.updateAccount(1, request)

        assertNotNull(response)
        assertEquals("Conta Decimal", response.name)
        assertEquals("1234.57", response.balance)

        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve atualizar conta com nome longo (boundary test)")
    fun `should update account with long name`() {
        val longName = "B".repeat(100) // Nome de 100 caracteres (limite da coluna)
        val request = UpdateAccountRequest(
            name = longName,
            balance = "1500.00",
            userId = 1
        )

        val accountWithLongName = existingAccount.copy()
        accountWithLongName.accountName = longName
        accountWithLongName.accountInitialBalance = BigDecimal("1500.00").setScale(2, RoundingMode.HALF_EVEN)

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(accountWithLongName)

        val response = updateAccountService.updateAccount(1, request)

        assertNotNull(response)
        assertEquals(longName, response.name)
        assertEquals("1500.00", response.balance)

        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve atualizar conta com valor monetário grande (edge test)")
    fun `should update account with large monetary value`() {
        val request = UpdateAccountRequest(
            name = "Conta Grande",
            balance = "99999999.99", // Valor máximo para precision 10, scale 2
            userId = 1
        )

        val accountWithLargeBalance = existingAccount.copy()
        accountWithLargeBalance.accountName = "Conta Grande"
        accountWithLargeBalance.accountInitialBalance = BigDecimal("99999999.99").setScale(2, RoundingMode.HALF_EVEN)

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(accountWithLargeBalance)

        val response = updateAccountService.updateAccount(1, request)

        assertNotNull(response)
        assertEquals("Conta Grande", response.name)
        assertEquals("99999999.99", response.balance)

        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve atualizar conta com valor negativo")
    fun `should update account with negative balance`() {
        val request = UpdateAccountRequest(
            name = "Conta Negativa",
            balance = "-1000.50",
            userId = 1
        )

        val accountWithNegativeBalance = existingAccount.copy()
        accountWithNegativeBalance.accountName = "Conta Negativa"
        accountWithNegativeBalance.accountInitialBalance = BigDecimal("-1000.50").setScale(2, RoundingMode.HALF_EVEN)

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(accountWithNegativeBalance)

        val response = updateAccountService.updateAccount(1, request)

        assertNotNull(response)
        assertEquals("Conta Negativa", response.name)
        assertEquals("-1000.50", response.balance)

        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve alterar apenas o nome da conta mantendo outros dados")
    fun `should update only account name keeping other data`() {
        val request = UpdateAccountRequest(
            name = "Novo Nome",
            balance = existingAccount.accountInitialBalance?.toString(),
            userId = 1
        )

        val accountWithNewName = existingAccount.copy()
        accountWithNewName.accountName = "Novo Nome"

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(accountWithNewName)

        val response = updateAccountService.updateAccount(1, request)

        assertNotNull(response)
        assertEquals("Novo Nome", response.name)
        assertEquals("1000.00", response.balance)
        assertEquals(1, response.userId)

        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve transferir conta para outro usuário")
    fun `should transfer account to another user`() {
        val request = UpdateAccountRequest(
            name = existingAccount.accountName,
            balance = existingAccount.accountInitialBalance?.toString(),
            userId = 2
        )

        val transferredAccount = existingAccount.copy()
        transferredAccount.userId = newUser

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(2)).thenReturn(Optional.of(newUser))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(transferredAccount)

        val response = updateAccountService.updateAccount(1, request)

        assertNotNull(response)
        assertEquals(existingAccount.accountName, response.name)
        assertEquals(existingAccount.accountInitialBalance?.toString(), response.balance)
        assertEquals(2, response.userId)

        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(2)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve atualizar apenas a descrição da conta")
    fun `should update only account description`() {
        val request = UpdateAccountRequest(
            name = existingAccount.accountName,
            description = "Nova descrição da conta",
            balance = existingAccount.accountInitialBalance?.toString(),
            userId = 1
        )

        val accountWithNewDescription = existingAccount.copy()
        accountWithNewDescription.accountDescription = "Nova descrição da conta"

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(accountWithNewDescription)

        val response = updateAccountService.updateAccount(1, request)

        assertNotNull(response)
        assertEquals(existingAccount.accountName, response.name)
        assertEquals("Nova descrição da conta", response.description)
        assertEquals(existingAccount.accountInitialBalance?.toString(), response.balance)

        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve atualizar a moeda da conta")
    fun `should update account currency`() {
        val request = UpdateAccountRequest(
            name = existingAccount.accountName,
            balance = existingAccount.accountInitialBalance?.toString(),
            currency = "EUR",
            userId = 1
        )

        val accountWithNewCurrency = existingAccount.copy()
        accountWithNewCurrency.accountCurrency = "EUR"

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(accountWithNewCurrency)

        val response = updateAccountService.updateAccount(1, request)

        assertNotNull(response)
        assertEquals(existingAccount.accountName, response.name)
        assertEquals("EUR", response.currency)
        assertEquals(existingAccount.accountInitialBalance?.toString(), response.balance)

        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve atualizar conta com descrição longa (boundary test)")
    fun `should update account with long description`() {
        val longDescription = "D".repeat(255) // Descrição de 255 caracteres (limite da coluna)
        val request = UpdateAccountRequest(
            name = "Conta com Descrição Longa",
            description = longDescription,
            balance = "500.00",
            userId = 1
        )

        val accountWithLongDescription = existingAccount.copy()
        accountWithLongDescription.accountName = "Conta com Descrição Longa"
        accountWithLongDescription.accountDescription = longDescription
        accountWithLongDescription.accountInitialBalance = BigDecimal("500.00").setScale(2, RoundingMode.HALF_EVEN)

        `when`(accountRepository.findById(1)).thenReturn(Optional.of(existingAccount))
        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(accountWithLongDescription)

        val response = updateAccountService.updateAccount(1, request)

        assertNotNull(response)
        assertEquals("Conta com Descrição Longa", response.name)
        assertEquals(longDescription, response.description)
        assertEquals("500.00", response.balance)

        verify(accountRepository).findById(1)
        verify(appUserRepository).findById(1)
        verify(accountRepository).save(any(Account::class.java))
    }
}
