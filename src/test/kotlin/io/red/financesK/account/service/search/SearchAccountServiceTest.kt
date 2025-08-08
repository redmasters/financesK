package io.red.financesK.account.service.search

import io.red.financesK.account.model.Account
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class SearchAccountServiceTest {

    private val accountRepository: AccountRepository = mock()
    private val appUserRepository: AppUserRepository = mock()

    private lateinit var service: SearchAccountService
    private lateinit var user: AppUser
    private lateinit var account1: Account
    private lateinit var account2: Account

    @BeforeEach
    fun setup() {
        service = SearchAccountService(accountRepository, appUserRepository)

        user = AppUser(1, "testUser", "test@user.com", "hash", Instant.now())

        account1 = Account(
            accountId = 1,
            accountName = "Conta Corrente",
            accountDescription = "Conta principal",
            accountInitialBalance = BigDecimal("1000.00"),
            accountCurrency = "BRL",
            userId = user
        )

        account2 = Account(
            accountId = 2,
            accountName = "Conta Poupança",
            accountDescription = "Conta para poupança",
            accountInitialBalance = BigDecimal("500.00"),
            accountCurrency = "BRL",
            userId = user
        )
    }

    @Test
    @DisplayName("Deve buscar contas por usuário com sucesso")
    fun `should search accounts by user id successfully`() {
        val accounts = listOf(account1, account2)

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.findAllByUserId(user)).thenReturn(accounts)

        val result = service.searchAccountsByUserId(1)

        assertEquals(2, result.size)
        assertEquals("Conta Corrente", result[0].name)
        assertEquals("Conta Poupança", result[1].name)
        assertEquals("1000.00", result[0].balance)
        assertEquals("500.00", result[1].balance)
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não possui contas")
    fun `should return empty list when user has no accounts`() {
        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.findAllByUserId(user)).thenReturn(emptyList())

        val result = service.searchAccountsByUserId(1)

        assertTrue(result.isEmpty())
    }

    @Test
    @DisplayName("Deve falhar ao buscar contas de usuário inexistente")
    fun `should fail when searching accounts for non-existent user`() {
        `when`(appUserRepository.findById(999)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            service.searchAccountsByUserId(999)
        }
        assertEquals("User with id 999 not found", exception.message)
    }

    @Test
    @DisplayName("Deve buscar conta por ID com sucesso")
    fun `should search account by id successfully`() {
        `when`(accountRepository.findById(1)).thenReturn(Optional.of(account1))

        val result = service.searchAccountById(1)

        assertEquals(1, result.accountId)
        assertEquals("Conta Corrente", result.accountName)
        assertEquals(BigDecimal("1000.00"), result.accountInitialBalance)
    }

    @Test
    @DisplayName("Deve falhar ao buscar conta inexistente por ID")
    fun `should fail when searching non-existent account by id`() {
        `when`(accountRepository.findById(999)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            service.searchAccountById(999)
        }
        assertEquals("Account with id 999 not found", exception.message)
    }

    @Test
    @DisplayName("Deve falhar ao buscar conta com ID nulo")
    fun `should fail when searching account with null id`() {
        val exception = assertThrows<IllegalArgumentException> {
            service.searchAccountById(null)
        }
        assertEquals("Account ID cannot be null", exception.message)
    }

    @Test
    @DisplayName("Deve buscar múltiplas contas do mesmo usuário")
    fun `should search multiple accounts for same user`() {
        val moreAccounts = listOf(
            account1,
            account2,
            Account(
                accountId = 3,
                accountName = "Conta Investimento",
                accountDescription = "Conta para investimentos",
                accountInitialBalance = BigDecimal("2000.00"),
                accountCurrency = "USD",
                userId = user
            )
        )

        `when`(appUserRepository.findById(1)).thenReturn(Optional.of(user))
        `when`(accountRepository.findAllByUserId(user)).thenReturn(moreAccounts)

        val result = service.searchAccountsByUserId(1)

        assertEquals(3, result.size)
        assertEquals("Conta Corrente", result[0].name)
        assertEquals("Conta Poupança", result[1].name)
        assertEquals("Conta Investimento", result[2].name)
        assertEquals("USD", result[2].currency)
    }

    @Test
    @DisplayName("Deve buscar saldos por lista de IDs de conta")
    fun `should fetch balances by account id list`() {
        val accountIds = listOf(1, 2, 3)
        val account3 = Account(
            accountId = 3,
            accountName = "Conta Investimento",
            accountDescription = "Conta para investimentos",
            accountInitialBalance = BigDecimal("2000.00"),
            accountCurrency = "USD",
            userId = user
        )
        val accounts = listOf(account1, account2, account3)

        `when`(accountRepository.findAllById(accountIds)).thenReturn(accounts)

        val result = service.getBalanceByAccountList(accountIds)
        assertEquals(3, result.size)
        assertEquals("1000.00", result[1].balance)
        assertEquals("500.00", result[2].balance)
        assertEquals("2000.00", result[3].balance)


    }

    @Test
    @DisplayName("Deve retornar somente 1 saldo quando apenas 1 ID de conta é fornecido")
    fun `should return only 1 balance when only 1 account id is provided`() {
        val accountIds = listOf(1)
        `when`(accountRepository.findAllById(accountIds)).thenReturn(listOf(account1))

        val result = service.getBalanceByAccountList(accountIds)

        assertEquals(1, result.size)
        assertEquals("1000.00", result[1])
    }

}
