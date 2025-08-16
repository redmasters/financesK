package io.red.financesK.transaction.event

import io.red.financesK.account.model.Account
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.user.model.AppUser
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class TransactionEventValidationTest {

    @Autowired
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @MockBean
    private lateinit var accountRepository: AccountRepository

    @Test
    @DisplayName("Deve validar que os eventos de transação estão sendo processados pelo handler")
    fun `should validate transaction events are being processed by handler`() {
        // Given
        val user = AppUser(
            id = 1,
            username = "testuser",
            email = "test@test.com",
            passwordHash = "hash"
        )

        val account = Account(
            accountId = 1,
            accountName = "Test Account",
            accountDescription = "Test",
            accountCurrentBalance = 1000,
            accountCurrency = "BRL",
            userId = user
        )

        // Mock repository behavior
        `when`(accountRepository.findById(1)).thenReturn(Optional.of(account))
        `when`(accountRepository.save(any(Account::class.java))).thenAnswer { invocation ->
            val accountToSave = invocation.getArgument<Account>(0)
            println("✅ Account balance updated to: ${accountToSave.accountCurrentBalance}")
            accountToSave
        }

        // When - Publish event
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = 1,
            amount = 500,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID
        )

        println("📤 Publishing event: $event")
        applicationEventPublisher.publishEvent(event)

        // Give some time for async processing
        Thread.sleep(1000)

        // Then - Verify interactions
        verify(accountRepository, atLeastOnce()).findById(1)
        verify(accountRepository, atLeastOnce()).save(any(Account::class.java))

        println("✅ EVENT PROCESSING VALIDATED SUCCESSFULLY!")
        println("🎯 Events are working correctly!")
    }
}
