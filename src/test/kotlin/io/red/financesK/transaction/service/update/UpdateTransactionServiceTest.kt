package io.red.financesK.transaction.service.update

import io.red.financesK.account.model.Account
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.transaction.controller.request.UpdateTransactionRequest
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.model.Category
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.repository.CategoryRepository
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*

class UpdateTransactionServiceTest {

    private val transactionRepository: TransactionRepository = mock()
    private val categoryRepository: CategoryRepository = mock()
    private val appUserRepository: AppUserRepository = mock()

    private lateinit var service: UpdateTransactionService
    private lateinit var category: Category
    private lateinit var newCategory: Category
    private lateinit var user: AppUser
    private lateinit var account: Account
    private lateinit var existingTransaction: Transaction

    @BeforeEach
    fun setup() {
        service = UpdateTransactionService(
            transactionRepository,
            categoryRepository,
            appUserRepository,
            updateBalanceService
        )

        category = Category(id = 1, name = "Alimentação", type = "EXPENSE")
        newCategory = Category(id = 2, name = "Transporte", type = "EXPENSE")
        user = AppUser(10, "testUser", "test@test.com", "hash", Instant.now())
        account = Account(
            accountId = 1,
            accountName = "Conta Corrente",
            accountDescription = "Conta principal",
            accountInitialBalance = BigDecimal("1000.00"),
            accountCurrency = "BRL",
            userId = user
        )

        existingTransaction = Transaction(
            id = 1,
            description = "Compra original",
            amount = BigDecimal("100.00"),
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PENDING,
            categoryId = category,
            dueDate = LocalDate.of(2025, 8, 3),
            createdAt = Instant.now(),
            notes = "Nota original",
            recurrencePattern = null,
            installmentInfo = null,
            userId = user,
            accountId = account
        )
    }

    @Test
    @DisplayName("Deve atualizar transação com sucesso")
    fun `should update transaction successfully`() {
        val request = UpdateTransactionRequest(
            description = "Compra atualizada",
            amount = BigDecimal("150.00"),
            type = "INCOME",
            status = "PAID",
            categoryId = 2,
            dueDate = LocalDate.of(2025, 8, 5),
            notes = "Nota atualizada"
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))
        `when`(categoryRepository.findById(2)).thenReturn(Optional.of(newCategory))
        `when`(transactionRepository.save(any(Transaction::class.java))).thenAnswer { invocation ->
            invocation.getArgument(0)
        }

        val result = service.execute(1, request)

        assertEquals("Compra atualizada", result.description)
        assertEquals(BigDecimal("150.00"), result.amount)
        assertEquals("INCOME", result.type)
        assertEquals("PAID", result.status)
        assertEquals(2, result.categoryId)
        assertEquals("Nota atualizada", result.notes)
        verify(transactionRepository).save(any(Transaction::class.java))
    }

    @Test
    @DisplayName("Deve atualizar parcialmente uma transação")
    fun `should partially update transaction`() {
        val request = UpdateTransactionRequest(
            amount = BigDecimal("200.00"),
            status = "PAID"
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))
        `when`(transactionRepository.save(any(Transaction::class.java))).thenAnswer { invocation ->
            invocation.getArgument(0)
        }

        val result = service.execute(1, request)

        // Campos atualizados
        assertEquals(BigDecimal("200.00"), result.amount)
        assertEquals("PAID", result.status)

        // Campos mantidos
        assertEquals("Compra original", result.description)
        assertEquals("EXPENSE", result.type)
        assertEquals(1, result.categoryId)
    }

    @Test
    @DisplayName("Deve falhar ao atualizar transação inexistente")
    fun `should fail when updating non-existent transaction`() {
        val request = UpdateTransactionRequest(
            description = "Nova descrição"
        )

        `when`(transactionRepository.findById(999)).thenReturn(Optional.empty())

        val exception = assertThrows<ValidationException> {
            service.execute(999, request)
        }
        assertEquals("Transaction with id 999 not found", exception.message)
    }

    @Test
    @DisplayName("Deve falhar ao atualizar com valor zero")
    fun `should fail when updating with zero amount`() {
        val request = UpdateTransactionRequest(
            amount = BigDecimal.ZERO
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))

        val exception = assertThrows<ValidationException> {
            service.execute(1, request)
        }
        assertEquals("Transaction amount must be greater than zero", exception.message)
    }

    @Test
    @DisplayName("Deve falhar ao atualizar com categoria inexistente")
    fun `should fail when updating with non-existent category`() {
        val request = UpdateTransactionRequest(
            categoryId = 999
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))
        `when`(categoryRepository.findById(999)).thenReturn(Optional.empty())

        val exception = assertThrows<ValidationException> {
            service.execute(1, request)
        }
        assertEquals("Category with id 999 not found", exception.message)
    }

    @Test
    @DisplayName("Deve atualizar informações de parcelamento")
    fun `should update installment information`() {
        val request = UpdateTransactionRequest(
            totalInstallments = 6,
            currentInstallment = 3
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))
        `when`(transactionRepository.save(any(Transaction::class.java))).thenAnswer { invocation ->
            invocation.getArgument(0)
        }

        val result = service.execute(1, request)

        assertNotNull(result.installmentInfo)
        assertEquals(6, result.installmentInfo?.totalInstallments)
        assertEquals(3, result.installmentInfo?.currentInstallment)
        assertEquals(BigDecimal("100.00"), result.installmentInfo?.installmentValue)
    }

    @Test
    @DisplayName("Deve falhar quando parcela atual é maior que total")
    fun `should fail when current installment is greater than total`() {
        val request = UpdateTransactionRequest(
            totalInstallments = 3,
            currentInstallment = 5
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))

        val exception = assertThrows<ValidationException> {
            service.execute(1, request)
        }
        assertEquals("Current installment cannot be greater than total installments", exception.message)
    }

    @Test
    @DisplayName("Deve atualizar com valor negativo e falhar")
    fun `should fail when updating with negative amount`() {
        val request = UpdateTransactionRequest(
            amount = BigDecimal("-50.00")
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))

        val exception = assertThrows<ValidationException> {
            service.execute(1, request)
        }
        assertEquals("Transaction amount must be greater than zero", exception.message)
    }

    @Test
    @DisplayName("Deve atualizar tipo de transação com valor inválido")
    fun `should fail when updating with invalid transaction type`() {
        val request = UpdateTransactionRequest(
            type = "INVALID_TYPE"
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))

        val exception = assertThrows<ValidationException> {
            service.execute(1, request)
        }
        assertEquals("Invalid transaction type: INVALID_TYPE", exception.message)
    }

    @Test
    @DisplayName("Deve atualizar status com valor inválido")
    fun `should fail when updating with invalid payment status`() {
        val request = UpdateTransactionRequest(
            status = "INVALID_STATUS"
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))

        val exception = assertThrows<ValidationException> {
            service.execute(1, request)
        }
        assertEquals("Invalid payment status: INVALID_STATUS", exception.message)
    }

    @Test
    @DisplayName("Deve atualizar padrão de recorrência")
    fun `should update recurrence pattern`() {
        val request = UpdateTransactionRequest(
            recurrencePattern = "MONTHLY"
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))
        `when`(transactionRepository.save(any(Transaction::class.java))).thenAnswer { invocation ->
            invocation.getArgument(0)
        }

        val result = service.execute(1, request)

        assertEquals("MONTHLY", result.recurrencePattern)
    }

    @Test
    @DisplayName("Deve atualizar entrada (downPayment)")
    fun `should update down payment`() {
        val transactionWithDownPayment = existingTransaction.copy(
            downPayment = BigDecimal("30.00")
        )
        val request = UpdateTransactionRequest(
            downPayment = BigDecimal("50.00")
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(transactionWithDownPayment))
        `when`(transactionRepository.save(any(Transaction::class.java))).thenAnswer { invocation ->
            invocation.getArgument(0)
        }

        val result = service.execute(1, request)

        assertEquals(BigDecimal("50.00"), result.downPayment)
    }
}
