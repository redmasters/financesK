package io.red.financesK.transaction.service.create

import io.mockk.*
import io.red.financesK.transaction.controller.request.CreateTransactionRequest
import io.red.financesK.transaction.model.Category
import io.red.financesK.transaction.model.InstallmentInfo
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.repository.CategoryRepository
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import net.bytebuddy.matcher.ElementMatchers.any
import org.hamcrest.Matchers.any
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*

class CreateTransactionServiceTest {

    val transactionRepository: TransactionRepository= mock()
    private val categoryRepository: CategoryRepository = mock()
    private val appUserRepository: AppUserRepository = mock()

    private lateinit var service: CreateTransactionService
    private lateinit var category: Category;
    private lateinit var user: AppUser;
    private lateinit var transaction: Transaction;

    @BeforeEach
    fun setup() {
        service = CreateTransactionService(transactionRepository, categoryRepository, appUserRepository)
        category = Category(id = 1, name = "Alimentação", type = "EXPENSE")
        user = AppUser(10, "testUser", "test@test", "hash", Instant.now())
        transaction = Transaction(
            id = 1,
            description = "Compra de supermercado 1/1",
            amount = BigDecimal("150.00"),
            type = Transaction.TransactionType.EXPENSE,
            categoryId = category,
            transactionDate = LocalDate.of(2025, 8, 3),
            createdAt = Instant.now(),
            notes = "Compra do mês",
            recurrencePattern = null,
            installmentInfo = InstallmentInfo(1, 1, BigDecimal("150.00")),
            userId = user
        )

    }

    @Test
    @DisplayName("Deve criar uma transação com sucesso")
    fun `should create transaction successfully`() {
        val request = CreateTransactionRequest(
            "Compra de supermercado",
            BigDecimal("150.00"),
            "EXPENSE",
            1,
            LocalDate.of(2025, 8, 3),
            "Compra do mês",
            null,
            1,
            10
        )
        val transactionList = listOf(transaction)

        `when`(categoryRepository.findById(request.categoryId)).thenReturn(Optional.of(category))
        `when`(appUserRepository.findById(request.userId)).thenReturn(Optional.of(user))
        `when`(transactionRepository.saveAll(transactionList)).thenReturn(transactionList)
        service.execute(request)

    }

}


