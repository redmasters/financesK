package io.red.financesK.transaction.service.update

import io.red.financesK.global.exception.ValidationException
import io.red.financesK.transaction.controller.request.UpdateTransactionRequest
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.model.Category
import io.red.financesK.transaction.model.InstallmentInfo
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
    private lateinit var existingTransaction: Transaction
    private lateinit var updatedTransaction: Transaction

    @BeforeEach
    fun setup() {
        service = UpdateTransactionService(transactionRepository, categoryRepository, appUserRepository)

        category = Category(id = 1, name = "Alimentação", type = "EXPENSE")
        newCategory = Category(id = 2, name = "Transporte", type = "EXPENSE")
        user = AppUser(10, "testUser", "test@test.com", "hash", Instant.now())

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
            installmentInfo = InstallmentInfo(1, 1, BigDecimal("100.00")),
            userId = user
        )

        updatedTransaction = Transaction(
            id = 1,
            description = "Compra atualizada",
            amount = BigDecimal("150.00"),
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID,
            categoryId = newCategory,
            dueDate = LocalDate.of(2025, 8, 5),
            createdAt = existingTransaction.createdAt,
            notes = "Nota atualizada",
            recurrencePattern = null,
            installmentInfo = InstallmentInfo(1, 1, BigDecimal("150.00")),
            userId = user
        )
    }

    @Test
    @DisplayName("Deve atualizar uma transação com sucesso")
    fun `should update transaction successfully`() {
        val request = UpdateTransactionRequest(
            description = "Compra atualizada",
            amount = BigDecimal("150.00"),
            categoryId = 2,
            dueDate = LocalDate.of(2025, 8, 5),
            notes = "Nota atualizada",
            status = "PAID"
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))
        `when`(categoryRepository.findById(2)).thenReturn(Optional.of(newCategory))
        `when`(transactionRepository.save(any(Transaction::class.java))).thenReturn(updatedTransaction)

        val result = service.execute(1, request)

        assertNotNull(result)
        assertEquals("Compra atualizada", result.description)
        assertEquals(BigDecimal("150.00"), result.amount)
        assertEquals(2, result.categoryId)
        assertEquals(LocalDate.of(2025, 8, 5), result.dueDate)
        assertEquals("Nota atualizada", result.notes)

        verify(transactionRepository).findById(1)
        verify(categoryRepository).findById(2)
        verify(transactionRepository).save(any(Transaction::class.java))
    }

    @Test
    @DisplayName("Deve atualizar apenas alguns campos da transação")
    fun `should update only some fields of transaction`() {
        val request = UpdateTransactionRequest(
            description = "Nova descrição",
            amount = BigDecimal("200.00")
        )

        val partiallyUpdatedTransaction = existingTransaction.copy(
            description = "Nova descrição",
            amount = BigDecimal("200.00")
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))
        `when`(transactionRepository.save(any(Transaction::class.java))).thenReturn(partiallyUpdatedTransaction)

        val result = service.execute(1, request)

        assertNotNull(result)
        assertEquals("Nova descrição", result.description)
        assertEquals(BigDecimal("200.00"), result.amount)
        assertEquals(1, result.categoryId) // Mantém categoria original
        assertEquals(existingTransaction.dueDate, result.dueDate) // Mantém data original

        verify(transactionRepository).findById(1)
        verify(transactionRepository).save(any(Transaction::class.java))
        verify(categoryRepository, never()).findById(any())
    }

    @Test
    @DisplayName("Deve lançar exceção quando transação não for encontrada")
    fun `should throw exception when transaction not found`() {
        val request = UpdateTransactionRequest(description = "Nova descrição")

        `when`(transactionRepository.findById(999)).thenReturn(Optional.empty())

        val exception = assertThrows<ValidationException> {
            service.execute(999, request)
        }

        assertEquals("Transaction with id 999 not found", exception.message)
        verify(transactionRepository).findById(999)
        verify(transactionRepository, never()).save(any(Transaction::class.java))
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor for zero ou negativo (boundary)")
    fun `should throw exception when amount is zero or negative`() {
        val request = UpdateTransactionRequest(amount = BigDecimal.ZERO)

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))

        val exception = assertThrows<ValidationException> {
            service.execute(1, request)
        }

        assertEquals("Transaction amount must be greater than zero", exception.message)
        verify(transactionRepository).findById(1)
        verify(transactionRepository, never()).save(any(Transaction::class.java))
    }

    @Test
    @DisplayName("Deve lançar exceção quando categoria não for encontrada")
    fun `should throw exception when category not found`() {
        val request = UpdateTransactionRequest(categoryId = 999)

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))
        `when`(categoryRepository.findById(999)).thenReturn(Optional.empty())

        val exception = assertThrows<ValidationException> {
            service.execute(1, request)
        }

        assertEquals("Category with id 999 not found", exception.message)
        verify(transactionRepository).findById(1)
        verify(categoryRepository).findById(999)
        verify(transactionRepository, never()).save(any(Transaction::class.java))
    }

    @Test
    @DisplayName("Deve lançar exceção quando tipo de transação for inválido")
    fun `should throw exception when transaction type is invalid`() {
        val request = UpdateTransactionRequest(type = "INVALID_TYPE")

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))

        val exception = assertThrows<ValidationException> {
            service.execute(1, request)
        }

        assertEquals("Invalid transaction type: INVALID_TYPE", exception.message)
        verify(transactionRepository).findById(1)
        verify(transactionRepository, never()).save(any(Transaction::class.java))
    }

    @Test
    @DisplayName("Deve lançar exceção quando status de pagamento for inválido")
    fun `should throw exception when payment status is invalid`() {
        val request = UpdateTransactionRequest(status = "INVALID_STATUS")

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))

        val exception = assertThrows<ValidationException> {
            service.execute(1, request)
        }

        assertEquals("Invalid payment status: INVALID_STATUS", exception.message)
        verify(transactionRepository).findById(1)
        verify(transactionRepository, never()).save(any(Transaction::class.java))
    }

    @Test
    @DisplayName("Deve lançar exceção quando padrão de recorrência for inválido")
    fun `should throw exception when recurrence pattern is invalid`() {
        val request = UpdateTransactionRequest(recurrencePattern = "INVALID_PATTERN")

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))

        val exception = assertThrows<ValidationException> {
            service.execute(1, request)
        }

        assertEquals("Invalid recurrence pattern: INVALID_PATTERN", exception.message)
        verify(transactionRepository).findById(1)
        verify(transactionRepository, never()).save(any(Transaction::class.java))
    }

    @Test
    @DisplayName("Deve atualizar informações de parcelas corretamente")
    fun `should update installment info correctly`() {
        val request = UpdateTransactionRequest(
            totalInstallments = 5,
            currentInstallment = 2,
            amount = BigDecimal("50.00")
        )

        val transactionWithInstallments = existingTransaction.copy(
            amount = BigDecimal("50.00"),
            installmentInfo = InstallmentInfo(5, 2, BigDecimal("50.00"))
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))
        `when`(transactionRepository.save(any(Transaction::class.java))).thenReturn(transactionWithInstallments)

        val result = service.execute(1, request)

        assertNotNull(result)
        assertEquals(BigDecimal("50.00"), result.amount)
        assertEquals(5, result.installmentInfo?.totalInstallments)
        assertEquals(2, result.installmentInfo?.currentInstallment)
        assertEquals(BigDecimal("50.00"), result.installmentInfo?.installmentValue)

        verify(transactionRepository).findById(1)
        verify(transactionRepository).save(any(Transaction::class.java))
    }

    @Test
    @DisplayName("Deve lançar exceção quando parcela atual for maior que total de parcelas (boundary)")
    fun `should throw exception when current installment is greater than total installments`() {
        val request = UpdateTransactionRequest(
            totalInstallments = 3,
            currentInstallment = 5
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(existingTransaction))

        val exception = assertThrows<ValidationException> {
            service.execute(1, request)
        }

        assertEquals("Current installment cannot be greater than total installments", exception.message)
        verify(transactionRepository).findById(1)
        verify(transactionRepository, never()).save(any(Transaction::class.java))
    }

    @Test
    @DisplayName("Deve atualizar transação parcelada mantendo informações originais quando não fornecidas")
    fun `should update installment transaction keeping original info when not provided`() {
        val originalTransactionWithInstallments = existingTransaction.copy(
            installmentInfo = InstallmentInfo(12, 3, BigDecimal("100.00"))
        )

        val request = UpdateTransactionRequest(
            description = "Descrição atualizada"
        )

        val updatedTransactionWithInstallments = originalTransactionWithInstallments.copy(
            description = "Descrição atualizada"
        )

        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(originalTransactionWithInstallments))
        `when`(transactionRepository.save(any(Transaction::class.java))).thenReturn(updatedTransactionWithInstallments)

        val result = service.execute(1, request)

        assertNotNull(result)
        assertEquals("Descrição atualizada", result.description)
        assertEquals(12, result.installmentInfo?.totalInstallments)
        assertEquals(3, result.installmentInfo?.currentInstallment)
        assertEquals(BigDecimal("100.00"), result.installmentInfo?.installmentValue)

        verify(transactionRepository).findById(1)
        verify(transactionRepository).save(any(Transaction::class.java))
    }
}
