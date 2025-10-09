package io.red.financesK.transaction.event

import io.red.financesK.account.model.Account
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.global.exception.NotFoundException
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class TransactionEventHandlerTest {

    @Mock
    private lateinit var accountRepository: AccountRepository

    @InjectMocks
    private lateinit var transactionEventHandler: TransactionEventHandler

    private lateinit var account: Account

    @BeforeEach
    fun setUp() {
        account = Account(
            accountId = 1,
            accountName = "Test Account",
            accountCurrentBalance = 1000
        )
    }

    @Test
    @DisplayName("Deve ignorar transação criada com status PENDING e não atualizar saldo")
    fun `should ignore pending transaction created and not update balance`() {
        // Given
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 500,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PENDING
        )

        // When
        transactionEventHandler.handleTransactionCreatedEvent(event)

        // Then
        verify(accountRepository, never()).findById(any())
        verify(accountRepository, never()).save(any())
    }

    @Test
    @DisplayName("Deve atualizar saldo quando transação criada com status PAID")
    fun `should update balance when transaction created with PAID status`() {
        // Given
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 500,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID
        )
        `when`(accountRepository.findById(1)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionCreatedEvent(event)

        // Then
        verify(accountRepository).findById(1)
        verify(accountRepository).save(any())
        assertEquals(500, account.accountCurrentBalance) // 1000 - 500 = 500 (expense)
    }

    @Test
    @DisplayName("Deve atualizar saldo quando transação criada com status FAILED")
    fun `should update balance when transaction created with FAILED status`() {
        // Given
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 300,
            type = TransactionType.INCOME,
            status = PaymentStatus.FAILED
        )
        `when`(accountRepository.findById(1)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionCreatedEvent(event)

        // Then
        verify(accountRepository).findById(1)
        verify(accountRepository).save(any())
        assertEquals(1300, account.accountCurrentBalance) // 1000 + 300 = 1300 (income)
    }

    @Test
    @DisplayName("Deve aplicar valor ao saldo quando status muda de PENDING para PAID")
    fun `should apply amount to balance when status changes from PENDING to PAID`() {
        // Given
        val event = TransactionStatusChangedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 200,
            type = TransactionType.EXPENSE,
            previousStatus = PaymentStatus.PENDING,
            newStatus = PaymentStatus.PAID
        )
        `when`(accountRepository.findById(1)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionStatusChangedEvent(event)

        // Then
        verify(accountRepository).findById(1)
        verify(accountRepository).save(any())
        assertEquals(800, account.accountCurrentBalance) // 1000 - 200 = 800 (expense)
    }

    @Test
    @DisplayName("Deve aplicar valor ao saldo quando status muda de FAILED para PAID")
    fun `should apply amount to balance when status changes from FAILED to PAID`() {
        // Given
        val event = TransactionStatusChangedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 150,
            type = TransactionType.INCOME,
            previousStatus = PaymentStatus.FAILED,
            newStatus = PaymentStatus.PAID
        )
        `when`(accountRepository.findById(1)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionStatusChangedEvent(event)

        // Then
        verify(accountRepository).findById(1)
        verify(accountRepository).save(any())
        assertEquals(1150, account.accountCurrentBalance) // 1000 + 150 = 1150 (income)
    }

    @Test
    @DisplayName("Deve reverter valor do saldo quando status muda de PAID para PENDING")
    fun `should revert amount from balance when status changes from PAID to PENDING`() {
        // Given
        val event = TransactionStatusChangedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 300,
            type = TransactionType.EXPENSE,
            previousStatus = PaymentStatus.PAID,
            newStatus = PaymentStatus.PENDING
        )
        `when`(accountRepository.findById(1)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionStatusChangedEvent(event)

        // Then
        verify(accountRepository).findById(1)
        verify(accountRepository).save(any())
        assertEquals(1300, account.accountCurrentBalance) // 1000 - (-300) = 1300 (reverting expense)
    }

    @Test
    @DisplayName("Deve reverter valor do saldo quando status muda de PAID para FAILED")
    fun `should revert amount from balance when status changes from PAID to FAILED`() {
        // Given
        val event = TransactionStatusChangedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 250,
            type = TransactionType.INCOME,
            previousStatus = PaymentStatus.PAID,
            newStatus = PaymentStatus.FAILED
        )
        `when`(accountRepository.findById(1)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionStatusChangedEvent(event)

        // Then
        verify(accountRepository).findById(1)
        verify(accountRepository).save(any())
        assertEquals(750, account.accountCurrentBalance) // 1000 - 250 = 750 (reverting income)
    }

    @Test
    @DisplayName("Não deve alterar saldo quando status permanece PENDING")
    fun `should not change balance when status remains PENDING`() {
        // Given
        val event = TransactionStatusChangedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 100,
            type = TransactionType.EXPENSE,
            previousStatus = PaymentStatus.PENDING,
            newStatus = PaymentStatus.PENDING
        )

        // When
        transactionEventHandler.handleTransactionStatusChangedEvent(event)

        // Then
        verify(accountRepository, never()).findById(any())
        verify(accountRepository, never()).save(any())
    }

    @Test
    @DisplayName("Não deve alterar saldo quando status permanece PAID")
    fun `should not change balance when status remains PAID`() {
        // Given
        val event = TransactionStatusChangedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 100,
            type = TransactionType.EXPENSE,
            previousStatus = PaymentStatus.PAID,
            newStatus = PaymentStatus.PAID
        )

        // When
        transactionEventHandler.handleTransactionStatusChangedEvent(event)

        // Then
        verify(accountRepository, never()).findById(any())
        verify(accountRepository, never()).save(any())
    }

    @Test
    @DisplayName("Não deve alterar saldo quando muda de PENDING para FAILED")
    fun `should not change balance when status changes from PENDING to FAILED`() {
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
        verify(accountRepository, never()).findById(any())
        verify(accountRepository, never()).save(any())
    }

    @Test
    @DisplayName("Não deve alterar saldo quando muda de FAILED para PENDING")
    fun `should not change balance when status changes from FAILED to PENDING`() {
        // Given
        val event = TransactionStatusChangedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 100,
            type = TransactionType.EXPENSE,
            previousStatus = PaymentStatus.FAILED,
            newStatus = PaymentStatus.PENDING
        )

        // When
        transactionEventHandler.handleTransactionStatusChangedEvent(event)

        // Then
        verify(accountRepository, never()).findById(any())
        verify(accountRepository, never()).save(any())
    }

    @Test
    @DisplayName("Deve lançar exceção quando conta não é encontrada")
    fun `should throw exception when account is not found`() {
        // Given
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = 999,
            amount = 100,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID
        )
        `when`(accountRepository.findById(999)).thenReturn(Optional.empty())

        // When & Then
        assertThrows<NotFoundException> {
            transactionEventHandler.handleTransactionCreatedEvent(event)
        }
    }

    @Test
    @DisplayName("Deve calcular corretamente saldo para despesa")
    fun `should correctly calculate balance for expense`() {
        // Given
        account.accountCurrentBalance = 500
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 200,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID
        )
        `when`(accountRepository.findById(1)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionCreatedEvent(event)

        // Then
        assertEquals(300, account.accountCurrentBalance) // 500 - 200 = 300
    }

    @Test
    @DisplayName("Deve calcular corretamente saldo para receita")
    fun `should correctly calculate balance for income`() {
        // Given
        account.accountCurrentBalance = 500
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 300,
            type = TransactionType.INCOME,
            status = PaymentStatus.PAID
        )
        `when`(accountRepository.findById(1)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionCreatedEvent(event)

        // Then
        assertEquals(800, account.accountCurrentBalance) // 500 + 300 = 800
    }

    @Test
    @DisplayName("Deve tratar saldo nulo corretamente")
    fun `should handle null balance correctly`() {
        // Given
        account.accountCurrentBalance = null
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 100,
            type = TransactionType.INCOME,
            status = PaymentStatus.PAID
        )
        `when`(accountRepository.findById(1)).thenReturn(Optional.of(account))

        // When
        transactionEventHandler.handleTransactionCreatedEvent(event)

        // Then
        assertEquals(100, account.accountCurrentBalance) // 0 + 100 = 100 (null treated as 0)
    }
}
