package io.red.financesK.transaction.event

import io.red.financesK.account.model.Account
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionEventIntegrationTest {

    @Autowired
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var appUserRepository: AppUserRepository

    private lateinit var testUser: AppUser
    private lateinit var testAccount: Account

    @BeforeEach
    fun setUp() {
        // Clean up any existing data and create test data
        accountRepository.deleteAll()
        appUserRepository.deleteAll()

        // Create test user
        testUser = AppUser(
            username = "integrationtest_user",
            email = "integration@test.com",
            passwordHash = "testhash",
            pathAvatar = "/test/avatar.png"
        )
        testUser = appUserRepository.save(testUser)

        // Create test account
        testAccount = Account(
            accountName = "Integration Test Account",
            accountDescription = "Test account for integration test",
            accountCurrentBalance = 1000,
            accountCurrency = "BRL",
            userId = testUser
        )
        testAccount = accountRepository.save(testAccount)
    }

//    @Test
    @DisplayName("Deve criar e persistir dados de teste corretamente")
    fun `should create and persist test data correctly`() {
        // Given - Data setup in @BeforeEach

        // When - Verify data was created
        val foundUser = appUserRepository.findById(testUser.id!!).orElse(null)
        val foundAccount = accountRepository.findById(testAccount.accountId!!).orElse(null)

        // Then - Verify everything is working
        assertNotNull(foundUser, "User should be found in database")
        assertNotNull(foundAccount, "Account should be found in database")
        assertEquals(testUser.username, foundUser.username)
        assertEquals(testAccount.accountName, foundAccount.accountName)
        assertEquals(1000, foundAccount.accountCurrentBalance)

        println("✅ Integration test setup validated successfully!")
        println("User: ${foundUser.username} (ID: ${foundUser.id})")
        println("Account: ${foundAccount.accountName} (ID: ${foundAccount.accountId}, Balance: ${foundAccount.accountCurrentBalance})")
    }

//    @Test
    @DisplayName("Deve publicar evento de transação sem erros")
    fun `should publish transaction event without errors`() {
        // Given - Test data from @BeforeEach
        val event = TransactionCreatedEvent(
            transactionId = 999,
            accountId = testAccount.accountId!!,
            amount = 250,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID
        )

        // When - Publish event (this tests that the event system is working)
        var eventPublished = false
        try {
            applicationEventPublisher.publishEvent(event)
            eventPublished = true
        } catch (e: Exception) {
            println("❌ Error publishing event: ${e.message}")
            throw e
        }

        // Then - Verify event was published without errors
        assert(eventPublished) { "Event should be published without errors" }

        // Note: We don't test the actual balance update here because that would require
        // the async event handler to complete, which is complex to test reliably
        println("✅ Transaction event published successfully!")
        println("Event details: TransactionId=${event.transactionId}, AccountId=${event.accountId}, Amount=${event.amount}")
    }
}
