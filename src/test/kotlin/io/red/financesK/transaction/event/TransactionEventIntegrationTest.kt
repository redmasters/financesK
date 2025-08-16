package io.red.financesK.transaction.event

import io.red.financesK.account.model.Account
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.user.model.AppUser
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionEventIntegrationTest {

    @Autowired
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Test
    @DisplayName("Deve processar evento de criação de transação e atualizar saldo da conta")
    fun `should process transaction created event and update account balance`() {
        // Given
        val user = AppUser(
            username = "testuser",
            email = "test@test.com",
            passwordHash = "hash"
        )

        val account = Account(
            accountName = "Test Account",
            accountDescription = "Test",
            accountCurrentBalance = 1000,
            accountCurrency = "BRL",
            userId = user
        )

        val savedAccount = accountRepository.save(account)
        val initialBalance = savedAccount.accountCurrentBalance

        // When
        val event = TransactionCreatedEvent(
            transactionId = 1,
            accountId = savedAccount.accountId ?: 0,
            amount = 500,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID
        )

        applicationEventPublisher.publishEvent(event)

        // Aguardar processamento do evento (por ser assíncrono)
        Thread.sleep(1000)

        // Then
        val updatedAccount = accountRepository.findById(savedAccount.accountId!!).orElse(null)
        val expectedBalance = initialBalance!! - 500

        assert(updatedAccount != null)
        assert(updatedAccount.accountCurrentBalance == expectedBalance) {
            "Expected balance: $expectedBalance, but was: ${updatedAccount.accountCurrentBalance}"
        }

        println("✅ Evento processado com sucesso!")
        println("Saldo inicial: $initialBalance")
        println("Saldo final: ${updatedAccount.accountCurrentBalance}")
    }
}
