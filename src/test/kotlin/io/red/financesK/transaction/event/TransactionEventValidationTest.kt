package io.red.financesK.transaction.event

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.red.financesK.account.model.Account
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.user.model.AppUser
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertNotNull

class TransactionEventValidationTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var transactionEventHandler: TransactionEventHandler

    @BeforeEach
    fun setUp() {
        accountRepository = mockk()
        transactionEventHandler = TransactionEventHandler(accountRepository)
    }

    @Test
    @DisplayName("Deve validar criação de evento de transação")
    fun `should validate transaction event creation`() {
        // Given
        val transactionId = 100
        val accountId = 1
        val amount = 500
        val type = TransactionType.EXPENSE
        val status = PaymentStatus.PAID

        // When
        val event = TransactionCreatedEvent(
            transactionId = transactionId,
            accountId = accountId,
            amount = amount,
            type = type,
            status = status
        )

        // Then - Verify event was created correctly
        assertNotNull(event, "Event should not be null")
        assert(event.transactionId == transactionId) { "Transaction ID should match" }
        assert(event.accountId == accountId) { "Account ID should match" }
        assert(event.amount == amount) { "Amount should match" }
        assert(event.type == type) { "Type should match" }
        assert(event.status == status) { "Status should match" }

        println("✅ Transaction event validation successful!")
        println("Event: $event")
    }

    @Test
    @DisplayName("Deve processar evento com mock do repositório")
    fun `should process event with mocked repository`() {
        // Given
        val user = AppUser(
            id = 1,
            username = "testuser",
            email = "test@test.com",
            passwordHash = "hash",
            pathAvatar = "/test/avatar.png"
        )

        val account = Account(
            accountId = 1,
            accountName = "Test Account",
            accountDescription = "Test Description",
            accountCurrentBalance = 1000,
            accountCurrency = "BRL",
            userId = user
        )

        val updatedAccount = account.copy(accountCurrentBalance = 500)

        // Mock repository behavior
        every { accountRepository.findById(1) } returns Optional.of(account)
        every { accountRepository.save(any<Account>()) } returns updatedAccount

        // When
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 500,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID
        )

        transactionEventHandler.handleTransactionCreatedEvent(event)

        // Then - Verify repository interactions
        verify { accountRepository.findById(1) }
        verify { accountRepository.save(any<Account>()) }

        println("✅ Event processing validation successful!")
        println("Repository interactions verified")
    }

    @Test
    @DisplayName("Deve validar diferentes tipos de transação")
    fun `should validate different transaction types`() {
        // Given & When & Then - Test different transaction types
        val expenseEvent = TransactionCreatedEvent(1, 1, 100, TransactionType.EXPENSE, PaymentStatus.PAID)
        val incomeEvent = TransactionCreatedEvent(2, 1, 200, TransactionType.INCOME, PaymentStatus.PAID)

        // Verify events can be created with different types
        assert(expenseEvent.type == TransactionType.EXPENSE) { "Should handle expense type" }
        assert(incomeEvent.type == TransactionType.INCOME) { "Should handle income type" }

        println("✅ Transaction type validation successful!")
        println("Expense event: ${expenseEvent.type}")
        println("Income event: ${incomeEvent.type}")
    }

    @Test
    @DisplayName("Deve validar diferentes status de pagamento")
    fun `should validate different payment statuses`() {
        // Given & When & Then - Test different payment statuses
        val paidEvent = TransactionCreatedEvent(1, 1, 100, TransactionType.EXPENSE, PaymentStatus.PAID)
        val pendingEvent = TransactionCreatedEvent(2, 1, 100, TransactionType.EXPENSE, PaymentStatus.PENDING)
        val failedEvent = TransactionCreatedEvent(3, 1, 100, TransactionType.EXPENSE, PaymentStatus.FAILED)

        // Verify events can be created with different statuses
        assert(paidEvent.status == PaymentStatus.PAID) { "Should handle paid status" }
        assert(pendingEvent.status == PaymentStatus.PENDING) { "Should handle pending status" }
        assert(failedEvent.status == PaymentStatus.FAILED) { "Should handle failed status" }

        println("✅ Payment status validation successful!")
        println("Payment statuses tested: ${PaymentStatus.values().joinToString()}")
    }

    @Test
    @DisplayName("Deve validar propriedades do evento")
    fun `should validate event properties`() {
        // Given
        val event1 = TransactionCreatedEvent(1, 100, 500, TransactionType.EXPENSE, PaymentStatus.PAID)
        val event2 = TransactionCreatedEvent(1, 100, 500, TransactionType.EXPENSE, PaymentStatus.PAID)

        // When & Then - Verify event equality and properties
        assert(event1.transactionId == event2.transactionId) { "Events with same data should have same transaction ID" }
        assert(event1.accountId == event2.accountId) { "Events with same data should have same account ID" }
        assert(event1.amount == event2.amount) { "Events with same data should have same amount" }
        assert(event1.type == event2.type) { "Events with same data should have same type" }
        assert(event1.status == event2.status) { "Events with same data should have same status" }

        println("✅ Event properties validation successful!")
    }
}
