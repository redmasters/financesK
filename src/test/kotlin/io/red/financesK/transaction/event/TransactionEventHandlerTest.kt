package io.red.financesK.transaction.event

import io.red.financesK.account.model.Account
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.global.exception.NotFoundException
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.user.model.AppUser
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.*

class TransactionEventHandlerTest {

    @Mock
    private lateinit var accountRepository: AccountRepository

    private lateinit var transactionEventHandler: TransactionEventHandler

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        transactionEventHandler = TransactionEventHandler(accountRepository)
    }

    @Test
    @DisplayName("Deve processar evento de criação de transação de despesa e atualizar saldo da conta")
    fun `should handle transaction created event for expense and update account balance`() {
        // Given
        val accountId = 1
        val amount = 100
        val initialBalance = 500
        val account = createAccount(accountId, initialBalance)
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = accountId,
            amount = amount,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID
        )

        `when`(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionCreatedEvent(event)

        // Then
        val accountCaptor = ArgumentCaptor.forClass(Account::class.java)
        verify(accountRepository).save(accountCaptor.capture())
        val savedAccount = accountCaptor.value

        assert(savedAccount.accountCurrentBalance == 400) // 500 - 100
        verify(accountRepository).findById(accountId)
    }

    @Test
    @DisplayName("Deve processar evento de criação de transação de receita e atualizar saldo da conta")
    fun `should handle transaction created event for income and update account balance`() {
        // Given
        val accountId = 1
        val amount = 200
        val initialBalance = 300
        val account = createAccount(accountId, initialBalance)
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = accountId,
            amount = amount,
            type = TransactionType.INCOME,
            status = PaymentStatus.PAID
        )

        `when`(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionCreatedEvent(event)

        // Then
        val accountCaptor = ArgumentCaptor.forClass(Account::class.java)
        verify(accountRepository).save(accountCaptor.capture())
        val savedAccount = accountCaptor.value

        assert(savedAccount.accountCurrentBalance == 500) // 300 + 200
        verify(accountRepository).findById(accountId)
    }

    @Test
    @DisplayName("Deve lançar exceção quando conta não for encontrada no evento de criação")
    fun `should throw exception when account not found on transaction created event`() {
        // Given
        val accountId = 999
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = accountId,
            amount = 100,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID
        )

        `when`(accountRepository.findById(accountId)).thenReturn(Optional.empty())

        // When & Then
        assertThrows<NotFoundException> {
            transactionEventHandler.handleTransactionCreatedEvent(event)
        }

        verify(accountRepository).findById(accountId)
        verify(accountRepository, never()).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve processar mudança de status de pendente para pago e atualizar saldo")
    fun `should handle status change from pending to paid and update balance`() {
        // Given
        val accountId = 1
        val amount = 150
        val initialBalance = 1000
        val account = createAccount(accountId, initialBalance)
        val event = TransactionStatusChangedEvent(
            transactionId = 1,
            accountId = accountId,
            amount = amount,
            type = TransactionType.EXPENSE,
            previousStatus = PaymentStatus.PENDING,
            newStatus = PaymentStatus.PAID
        )

        `when`(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionStatusChangedEvent(event)

        // Then
        val accountCaptor = ArgumentCaptor.forClass(Account::class.java)
        verify(accountRepository).save(accountCaptor.capture())
        val savedAccount = accountCaptor.value

        assert(savedAccount.accountCurrentBalance == 850) // 1000 - 150
        verify(accountRepository).findById(accountId)
    }

    @Test
    @DisplayName("Deve processar mudança de status de pago para pendente e reverter saldo")
    fun `should handle status change from paid to pending and revert balance`() {
        // Given
        val accountId = 1
        val amount = 75
        val initialBalance = 800
        val account = createAccount(accountId, initialBalance)
        val event = TransactionStatusChangedEvent(
            transactionId = 1,
            accountId = accountId,
            amount = amount,
            type = TransactionType.INCOME,
            previousStatus = PaymentStatus.PAID,
            newStatus = PaymentStatus.PENDING
        )

        `when`(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionStatusChangedEvent(event)

        // Then
        val accountCaptor = ArgumentCaptor.forClass(Account::class.java)
        verify(accountRepository).save(accountCaptor.capture())
        val savedAccount = accountCaptor.value

        assert(savedAccount.accountCurrentBalance == 725) // 800 - 75 (reversed income)
        verify(accountRepository).findById(accountId)
    }

    @Test
    @DisplayName("Não deve atualizar saldo quando mudança de status não envolve 'PAID'")
    fun `should not update balance when status change does not involve paid`() {
        // Given
        val event = TransactionStatusChangedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 100,
            type = TransactionType.EXPENSE,
            previousStatus = PaymentStatus.PENDING,
            newStatus = PaymentStatus.FAILED
        )

        // When
        transactionEventHandler.handleTransactionStatusChangedEvent(event)

        // Then
        verify(accountRepository, never()).findById(any(Int::class.java))
        verify(accountRepository, never()).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve lançar exceção quando conta não for encontrada no evento de mudança de status")
    fun `should throw exception when account not found on status change event`() {
        // Given
        val accountId = 999
        val event = TransactionStatusChangedEvent(
            transactionId = 1,
            accountId = accountId,
            amount = 100,
            type = TransactionType.EXPENSE,
            previousStatus = PaymentStatus.PENDING,
            newStatus = PaymentStatus.PAID
        )

        `when`(accountRepository.findById(accountId)).thenReturn(Optional.empty())

        // When & Then
        assertThrows<NotFoundException> {
            transactionEventHandler.handleTransactionStatusChangedEvent(event)
        }

        verify(accountRepository).findById(accountId)
        verify(accountRepository, never()).save(any(Account::class.java))
    }

    @Test
    @DisplayName("Deve processar transação de despesa com saldo inicial zero")
    fun `should handle expense transaction with zero initial balance`() {
        // Given
        val accountId = 1
        val amount = 50
        val account = createAccount(accountId, 0)
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = accountId,
            amount = amount,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID
        )

        `when`(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionCreatedEvent(event)

        // Then
        val accountCaptor = ArgumentCaptor.forClass(Account::class.java)
        verify(accountRepository).save(accountCaptor.capture())
        val savedAccount = accountCaptor.value

        assert(savedAccount.accountCurrentBalance == -50) // 0 - 50
    }

    @Test
    @DisplayName("Deve processar transação com saldo inicial nulo")
    fun `should handle transaction with null initial balance`() {
        // Given
        val accountId = 1
        val amount = 100
        val account = createAccount(accountId, null)
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = accountId,
            amount = amount,
            type = TransactionType.INCOME,
            status = PaymentStatus.PAID
        )

        `when`(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionCreatedEvent(event)

        // Then
        val accountCaptor = ArgumentCaptor.forClass(Account::class.java)
        verify(accountRepository).save(accountCaptor.capture())
        val savedAccount = accountCaptor.value

        assert(savedAccount.accountCurrentBalance == 100) // 0 + 100 (null treated as 0)
    }

    @Test
    @DisplayName("Deve processar transação com tipo nulo como receita")
    fun `should handle transaction with null type as income`() {
        // Given
        val accountId = 1
        val amount = 200
        val initialBalance = 500
        val account = createAccount(accountId, initialBalance)
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = accountId,
            amount = amount,
            type = null,
            status = PaymentStatus.PAID
        )

        `when`(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionCreatedEvent(event)

        // Then
        val accountCaptor = ArgumentCaptor.forClass(Account::class.java)
        verify(accountRepository).save(accountCaptor.capture())
        val savedAccount = accountCaptor.value

        assert(savedAccount.accountCurrentBalance == 700) // 500 + 200 (null type treated as income)
    }

    @Test
    @DisplayName("Deve processar mudança de status de pago para falha e reverter saldo")
    fun `should handle status change from paid to failed and revert balance`() {
        // Given
        val accountId = 1
        val amount = 300
        val initialBalance = 1500
        val account = createAccount(accountId, initialBalance)
        val event = TransactionStatusChangedEvent(
            transactionId = 1,
            accountId = accountId,
            amount = amount,
            type = TransactionType.EXPENSE,
            previousStatus = PaymentStatus.PAID,
            newStatus = PaymentStatus.FAILED
        )

        `when`(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionStatusChangedEvent(event)

        // Then
        val accountCaptor = ArgumentCaptor.forClass(Account::class.java)
        verify(accountRepository).save(accountCaptor.capture())
        val savedAccount = accountCaptor.value

        assert(savedAccount.accountCurrentBalance == 1800) // 1500 + 300 (reversed expense)
    }

    private fun createAccount(accountId: Int, initialBalance: Int?): Account {
        val user = AppUser(
            id = 1,
            username = "testuser",
            email = "test@test.com",
            passwordHash = "hashedpassword"
        )

        return Account(
            accountId = accountId,
            accountName = "Test Account",
            accountDescription = "Test Description",
            accountCurrentBalance = initialBalance,
            accountCurrency = "BRL",
            userId = user
        )
    }
}
