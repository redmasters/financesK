package io.red.financesK.account.balance.service.update

import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.account.balance.service.history.CreateBalanceHistory
import io.red.financesK.account.controller.response.UpdateAccountResponse
import io.red.financesK.account.model.Account
import io.red.financesK.account.service.search.SearchAccountService
import io.red.financesK.account.service.update.UpdateAccountService
import io.red.financesK.user.model.AppUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class OperationBalanceServiceTest {

    @Mock
    private lateinit var updateAccountService: UpdateAccountService

    @Mock
    private lateinit var searchAccountService: SearchAccountService

    @Mock
    private lateinit var createBalanceHistory: CreateBalanceHistory

    @InjectMocks
    private lateinit var service: OperationBalanceService

    private lateinit var user: AppUser
    private lateinit var account: Account

    @BeforeEach
    fun setup() {
        user = AppUser(1, "testUser", "test@user.com", "hash", Instant.now())
        account = Account(
            accountId = 1,
            accountName = "Conta Corrente",
            accountDescription = "Conta principal",
            accountInitialBalance = BigDecimal("1000.00"),
            accountCurrency = "BRL",
            userId = user
        )
    }

    @Test
    @DisplayName("Deve somar valor ao saldo com sucesso")
    fun `should add amount to balance successfully`() {
        // Given
        val amount = BigDecimal("500.00")
        val expectedNewBalance = BigDecimal("1500.00")
        val mockUpdateResponse = UpdateAccountResponse(
            accountId = 1,
            name = "Conta Corrente",
            description = "Conta principal",
            balance = "1500.00",
            currency = "BRL",
            userId = 1
        )

        // When
        `when`(searchAccountService.searchAccountById(1)).thenReturn(account)
        `when`(updateAccountService.updateAccountBalance(1, "1500.00")).thenReturn(mockUpdateResponse)
        doNothing().`when`(createBalanceHistory).createBalanceHistory(1, amount, AccountOperationType.DEPOSIT)

        // Then
        val result = service.sumBalance(1, amount, AccountOperationType.DEPOSIT)

        assertEquals(expectedNewBalance, result)
        verify(searchAccountService).searchAccountById(1)
        verify(updateAccountService).updateAccountBalance(1, "1500.00")
        verify(createBalanceHistory).createBalanceHistory(1, amount, AccountOperationType.DEPOSIT)
    }

    @Test
    @DisplayName("Deve subtrair valor do saldo com sucesso")
    fun `should subtract amount from balance successfully`() {
        // Given
        val amount = BigDecimal("300.00")
        val expectedNewBalance = BigDecimal("700.00")
        val mockUpdateResponse = UpdateAccountResponse(
            accountId = 1,
            name = "Conta Corrente",
            description = "Conta principal",
            balance = "700.00",
            currency = "BRL",
            userId = 1
        )

        // When
        `when`(searchAccountService.searchAccountById(1)).thenReturn(account)
        `when`(updateAccountService.updateAccountBalance(1, "700.00")).thenReturn(mockUpdateResponse)
        doNothing().`when`(createBalanceHistory).createBalanceHistory(1, amount, AccountOperationType.WITHDRAWAL)

        // Then
        val result = service.subtractBalance(1, amount, AccountOperationType.WITHDRAWAL,)

        assertEquals(expectedNewBalance, result)
        verify(searchAccountService).searchAccountById(1)
        verify(updateAccountService).updateAccountBalance(1, "700.00")
        verify(createBalanceHistory).createBalanceHistory(1, amount, AccountOperationType.WITHDRAWAL)
    }

    @Test
    @DisplayName("Deve permitir saldo negativo ao subtrair")
    fun `should allow negative balance when subtracting`() {
        // Given
        val amount = BigDecimal("1200.00")
        val expectedNewBalance = BigDecimal("-200.00")
        val mockUpdateResponse = UpdateAccountResponse(
            accountId = 1,
            name = "Conta Corrente",
            description = "Conta principal",
            balance = "-200.00",
            currency = "BRL",
            userId = 1
        )

        // When
        `when`(searchAccountService.searchAccountById(1)).thenReturn(account)
        `when`(updateAccountService.updateAccountBalance(1, "-200.00")).thenReturn(mockUpdateResponse)
        doNothing().`when`(createBalanceHistory).createBalanceHistory(1, amount, AccountOperationType.PAYMENT)

        // Then
        val result = service.subtractBalance(1, amount, AccountOperationType.PAYMENT,)

        assertEquals(expectedNewBalance, result)
        verify(searchAccountService).searchAccountById(1)
        verify(updateAccountService).updateAccountBalance(1, "-200.00")
        verify(createBalanceHistory).createBalanceHistory(1, amount, AccountOperationType.PAYMENT)
    }

    @Test
    @DisplayName("Deve falhar ao somar valor quando saldo atual é nulo")
    fun `should fail when adding amount with null current balance`() {
        // Given
        val accountWithNullBalance = account.copy(accountInitialBalance = null)
        val amount = BigDecimal("100.00")

        // When
        `when`(searchAccountService.searchAccountById(1)).thenReturn(accountWithNullBalance)

        // Then
        val exception = assertThrows<IllegalArgumentException> {
            service.sumBalance(1, amount, AccountOperationType.DEPOSIT)
        }
        assertEquals("Current balance is null for account with id 1", exception.message)
        verify(searchAccountService).searchAccountById(1)
        verifyNoInteractions(updateAccountService)
        verifyNoInteractions(createBalanceHistory)
    }

    @Test
    @DisplayName("Deve falhar ao subtrair valor quando saldo atual é nulo")
    fun `should fail when subtracting amount with null current balance`() {
        // Given
        val accountWithNullBalance = account.copy(accountInitialBalance = null)
        val amount = BigDecimal("100.00")

        // When
        `when`(searchAccountService.searchAccountById(1)).thenReturn(accountWithNullBalance)

        // Then
        val exception = assertThrows<IllegalArgumentException> {
            service.subtractBalance(1, amount, AccountOperationType.WITHDRAWAL,)
        }
        assertEquals("Current balance is null for account with id 1", exception.message)
        verify(searchAccountService).searchAccountById(1)
        verifyNoInteractions(updateAccountService)
        verifyNoInteractions(createBalanceHistory)
    }

    @Test
    @DisplayName("Deve processar transferência de entrada")
    fun `should process transfer in operation`() {
        // Given
        val amount = BigDecimal("250.00")
        val expectedNewBalance = BigDecimal("1250.00")
        val mockUpdateResponse = UpdateAccountResponse(
            accountId = 1,
            name = "Conta Corrente",
            description = "Conta principal",
            balance = "1250.00",
            currency = "BRL",
            userId = 1
        )

        // When
        `when`(searchAccountService.searchAccountById(1)).thenReturn(account)
        `when`(updateAccountService.updateAccountBalance(1, "1250.00")).thenReturn(mockUpdateResponse)
        doNothing().`when`(createBalanceHistory).createBalanceHistory(1, amount, AccountOperationType.TRANSFER_IN)

        // Then
        val result = service.sumBalance(1, amount, AccountOperationType.TRANSFER_IN)

        assertEquals(expectedNewBalance, result)
        verify(createBalanceHistory).createBalanceHistory(1, amount, AccountOperationType.TRANSFER_IN)
    }

    @Test
    @DisplayName("Deve processar transferência de saída")
    fun `should process transfer out operation`() {
        // Given
        val amount = BigDecimal("150.00")
        val expectedNewBalance = BigDecimal("850.00")
        val mockUpdateResponse = UpdateAccountResponse(
            accountId = 1,
            name = "Conta Corrente",
            description = "Conta principal",
            balance = "850.00",
            currency = "BRL",
            userId = 1
        )

        // When
        `when`(searchAccountService.searchAccountById(1)).thenReturn(account)
        `when`(updateAccountService.updateAccountBalance(1, "850.00")).thenReturn(mockUpdateResponse)
        doNothing().`when`(createBalanceHistory).createBalanceHistory(1, amount, AccountOperationType.TRANSFER_OUT)

        // Then
        val result = service.subtractBalance(1, amount, AccountOperationType.TRANSFER_OUT,)

        assertEquals(expectedNewBalance, result)
        verify(createBalanceHistory).createBalanceHistory(1, amount, AccountOperationType.TRANSFER_OUT)
    }

    @Test
    @DisplayName("Deve processar operação de juros")
    fun `should process interest operation`() {
        // Given
        val interestAmount = BigDecimal("50.00")
        val expectedNewBalance = BigDecimal("1050.00")
        val mockUpdateResponse = UpdateAccountResponse(
            accountId = 1,
            name = "Conta Corrente",
            description = "Conta principal",
            balance = "1050.00",
            currency = "BRL",
            userId = 1
        )

        // When
        `when`(searchAccountService.searchAccountById(1)).thenReturn(account)
        `when`(updateAccountService.updateAccountBalance(1, "1050.00")).thenReturn(mockUpdateResponse)
        doNothing().`when`(createBalanceHistory).createBalanceHistory(1, interestAmount, AccountOperationType.INTEREST)

        // Then
        val result = service.sumBalance(1, interestAmount, AccountOperationType.INTEREST)

        assertEquals(expectedNewBalance, result)
        verify(createBalanceHistory).createBalanceHistory(1, interestAmount, AccountOperationType.INTEREST)
    }

    @Test
    @DisplayName("Deve processar operação de taxa")
    fun `should process fee operation`() {
        // Given
        val feeAmount = BigDecimal("25.00")
        val expectedNewBalance = BigDecimal("975.00")
        val mockUpdateResponse = UpdateAccountResponse(
            accountId = 1,
            name = "Conta Corrente",
            description = "Conta principal",
            balance = "975.00",
            currency = "BRL",
            userId = 1
        )

        // When
        `when`(searchAccountService.searchAccountById(1)).thenReturn(account)
        `when`(updateAccountService.updateAccountBalance(1, "975.00")).thenReturn(mockUpdateResponse)
        doNothing().`when`(createBalanceHistory).createBalanceHistory(1, feeAmount, AccountOperationType.FEE)

        // Then
        val result = service.subtractBalance(1, feeAmount, AccountOperationType.FEE,)

        assertEquals(expectedNewBalance, result)
        verify(createBalanceHistory).createBalanceHistory(1, feeAmount, AccountOperationType.FEE)
    }

    @Test
    @DisplayName("Deve falhar quando ID da conta for nulo na soma")
    fun `should fail when account id is null for sum operation`() {
        // Given
        val amount = BigDecimal("100.00")

        // When
        `when`(searchAccountService.searchAccountById(null)).thenThrow(
            IllegalArgumentException("Account ID cannot be null")
        )

        // Then
        val exception = assertThrows<IllegalArgumentException> {
            service.sumBalance(null, amount, AccountOperationType.DEPOSIT)
        }
        assertEquals("Account ID cannot be null", exception.message)
        verify(searchAccountService).searchAccountById(null)
        verifyNoInteractions(updateAccountService)
        verifyNoInteractions(createBalanceHistory)
    }

    @Test
    @DisplayName("Deve falhar quando ID da conta for nulo na subtração")
    fun `should fail when account id is null for subtract operation`() {
        // Given
        val amount = BigDecimal("100.00")

        // When
        `when`(searchAccountService.searchAccountById(null)).thenThrow(
            IllegalArgumentException("Account ID cannot be null")
        )

        // Then
        val exception = assertThrows<IllegalArgumentException> {
            service.subtractBalance(null, amount, AccountOperationType.WITHDRAWAL,)
        }
        assertEquals("Account ID cannot be null", exception.message)
        verify(searchAccountService).searchAccountById(null)
        verifyNoInteractions(updateAccountService)
        verifyNoInteractions(createBalanceHistory)
    }
}
