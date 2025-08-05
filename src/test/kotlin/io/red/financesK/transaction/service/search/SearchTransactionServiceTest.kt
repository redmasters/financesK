package io.red.financesK.transaction.service.search

import io.red.financesK.transaction.controller.request.SearchTransactionFilter
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.model.Category
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.model.InstallmentInfo
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.transaction.repository.custom.TransactionCustomRepository
import io.red.financesK.user.model.AppUser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Instant
import java.util.Optional
import io.red.financesK.transaction.enums.PaymentStatus

@ExtendWith(MockitoExtension::class)
class SearchTransactionServiceTest {

    @Mock
    private lateinit var transactionCustomRepository: TransactionCustomRepository
    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @InjectMocks
    private lateinit var searchTransactionService: SearchTransactionService

    private var user: AppUser = AppUser(1, "testUser", "test@user", "hash", Instant.now());
    private var category: Category = Category(id = 1, name = "Alimentação", type = "EXPENSE")

    @Test
    @DisplayName("Deve buscar transações por apenas um campo (description)")
    fun `should search transactions by single field`() {
        val filter = SearchTransactionFilter(
            description = "Compra"
        )
        val transaction = Transaction(
            id = 1,
            description = "Compra",
            amount = BigDecimal(100.0),
            downPayment = null,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PENDING,
            categoryId = category,
            dueDate = LocalDate.of(2025, 8, 4),
            createdAt = Instant.now(),
            userId = user,
            installmentInfo = null,
            notes = null,
            recurrencePattern = null
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction), pageable, 1)
        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)
        assertEquals(1, result.totalElements)
        assertEquals("Compra", result.content[0].description)
        assertEquals("PENDING", result.content[0].status)
    }

    @Test
    @DisplayName("Deve buscar transações por múltiplos campos")
    fun `should search transactions by multiple fields`() {
        val filter = SearchTransactionFilter(
            description = "Compra",
            amount = BigDecimal(100.0),
            type = "EXPENSE",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 4),
            notes = "Nota",
            userId = 1
        )
        val transaction = Transaction(
            id = 1,
            description = "Compra",
            amount = BigDecimal(100.0),
            downPayment = BigDecimal(20.0),
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID,
            categoryId = category,
            dueDate = LocalDate.of(2025, 8, 4),
            createdAt = Instant.now(),
            userId = user,
            installmentInfo = null,
            notes = "Nota",
            recurrencePattern = null
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction), pageable, 1)
        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)
        assertEquals(1, result.totalElements)
        assertEquals("Compra", result.content[0].description)
        assertEquals(BigDecimal(100.0), result.content[0].amount)
        assertEquals(BigDecimal(20.0), result.content[0].downPayment)
        assertEquals("PAID", result.content[0].status)
        assertEquals("Nota", result.content[0].notes)
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não houver transações")
    fun `should return empty page when no transactions found`() {
        val filter = SearchTransactionFilter(
            description = "Inexistente",
            amount = BigDecimal(999.0),
            type = "EXPENSE",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 4),
            userId = 1
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(emptyList<Transaction>(), pageable, 0)
        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)
        assertEquals(0, result.totalElements)
        assertTrue(result.content.isEmpty())
    }

    @Test
    @DisplayName("Deve buscar transação parcelada corretamente")
    fun `should search installment transaction correctly`() {
        val installmentInfo = InstallmentInfo(
            totalInstallments = 12,
            currentInstallment = 1,
            installmentValue = BigDecimal(10.0)
        )
        val filter = SearchTransactionFilter(
            description = "Compra parcelada",
            amount = BigDecimal(120.0),
            type = "EXPENSE",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 4),
            totalInstallments = 12,
            userId = 1
        )
        val transaction = Transaction(
            id = 2,
            description = "Compra parcelada",
            amount = BigDecimal(120.0),
            downPayment = BigDecimal(20.0),
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PENDING,
            categoryId = category,
            dueDate = LocalDate.of(2025, 8, 4),
            createdAt = Instant.now(),
            userId = user,
            installmentInfo = installmentInfo,
            notes = null,
            recurrencePattern = null
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction), pageable, 1)
        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)
        assertEquals(1, result.totalElements)
        assertEquals(installmentInfo, result.content[0].installmentInfo)
        assertEquals(BigDecimal(20.0), result.content[0].downPayment)
        assertEquals("PENDING", result.content[0].status)
    }

    @Test
    @DisplayName("Deve retornar a transação pelo id quando existir")
    fun `should return transaction response when found`() {
        val transaction = Transaction(
            id = 1,
            description = "Compra supermercado",
            amount = BigDecimal("150.00"),
            downPayment = BigDecimal("30.00"),
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID,
            categoryId = Category(id = 1, name = "Supermercado", type = "EXPENSE"),
            dueDate = LocalDate.of(2025, 8, 3),
            createdAt = Instant.now(),
            notes = "Compra do mês",
            recurrencePattern = null,
            installmentInfo = null,
            userId = AppUser(42, "teste", "teste@teste.com", "hash", Instant.now())
        )
        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(transaction))
        val response = searchTransactionService.searchById(1)
        assertEquals(transaction.id, response?.id)
        assertEquals(transaction.description, response?.description)
        assertEquals(transaction.amount, response?.amount)
        assertEquals(transaction.downPayment, response?.downPayment)
        assertEquals(transaction.type?.name, response?.type)
        assertEquals(transaction.status.name, response?.status)
        assertEquals(transaction.categoryId.id, response?.categoryId)
        assertEquals(transaction.dueDate, response?.dueDate)
        assertEquals(transaction.createdAt, response?.createdAt)
        assertEquals(transaction.notes, response?.notes)
        assertEquals(transaction.recurrencePattern?.name, response?.recurrencePattern)
        assertEquals(transaction.installmentInfo, response?.installmentInfo)
        assertEquals(transaction.userId.id, response?.userId)
    }

    @Test
    @DisplayName("Deve retornar null quando não encontrar a transação pelo id")
    fun `should return null when transaction not found`() {
        `when`(transactionRepository.findById(99)).thenReturn(Optional.empty())
        val response = searchTransactionService.searchById(99)
        assertNull(response)
    }

    @Test
    @DisplayName("Deve buscar transações por período de data (startDate e endDate)")
    fun `should search transactions by date period`() {
        val filter = SearchTransactionFilter(
            startDate = LocalDate.of(2025, 8, 1),
            endDate = LocalDate.of(2025, 8, 31)
        )
        val transaction = Transaction(
            id = 1,
            description = "Transação no período",
            amount = BigDecimal(100.0),
            downPayment = null,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PENDING,
            categoryId = category,
            dueDate = LocalDate.of(2025, 8, 15),
            createdAt = Instant.now(),
            userId = user,
            installmentInfo = null,
            notes = null,
            recurrencePattern = null
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction), pageable, 1)
        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)
        assertEquals(1, result.totalElements)
        assertEquals(LocalDate.of(2025, 8, 15), result.content[0].dueDate)
    }

    @Test
    @DisplayName("Deve buscar apenas transações com entrada (hasDownPayment = true)")
    fun `should search only transactions with down payment`() {
        val filter = SearchTransactionFilter(
            hasDownPayment = true
        )
        val transaction = Transaction(
            id = 1,
            description = "Transação com entrada",
            amount = BigDecimal(1000.0),
            downPayment = BigDecimal(200.0),
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PENDING,
            categoryId = category,
            dueDate = LocalDate.of(2025, 8, 15),
            createdAt = Instant.now(),
            userId = user,
            installmentInfo = null,
            notes = null,
            recurrencePattern = null
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction), pageable, 1)
        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)
        assertEquals(1, result.totalElements)
        assertEquals(BigDecimal(200.0), result.content[0].downPayment)
    }

    @Test
    @DisplayName("Deve buscar apenas transações parceladas (isInstallment = true)")
    fun `should search only installment transactions`() {
        val installmentInfo = InstallmentInfo(
            totalInstallments = 6,
            currentInstallment = 1,
            installmentValue = BigDecimal(50.0)
        )
        val filter = SearchTransactionFilter(
            isInstallment = true
        )
        val transaction = Transaction(
            id = 1,
            description = "Transação parcelada",
            amount = BigDecimal(300.0),
            downPayment = null,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PENDING,
            categoryId = category,
            dueDate = LocalDate.of(2025, 8, 15),
            createdAt = Instant.now(),
            userId = user,
            installmentInfo = installmentInfo,
            notes = null,
            recurrencePattern = null
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction), pageable, 1)
        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)
        assertEquals(1, result.totalElements)
        assertEquals(6, result.content[0].installmentInfo?.totalInstallments)
    }

    @Test
    @DisplayName("Deve buscar transações por faixa de valor (minAmount e maxAmount)")
    fun `should search transactions by amount range`() {
        val filter = SearchTransactionFilter(
            minAmount = BigDecimal(50.0),
            maxAmount = BigDecimal(150.0)
        )
        val transaction = Transaction(
            id = 1,
            description = "Transação na faixa",
            amount = BigDecimal(100.0),
            downPayment = null,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PENDING,
            categoryId = category,
            dueDate = LocalDate.of(2025, 8, 15),
            createdAt = Instant.now(),
            userId = user,
            installmentInfo = null,
            notes = null,
            recurrencePattern = null
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction), pageable, 1)
        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)
        assertEquals(1, result.totalElements)
        assertEquals(BigDecimal(100.0), result.content[0].amount)
    }

    @Test
    @DisplayName("Deve buscar transações por status específico")
    fun `should search transactions by specific status`() {
        val filter = SearchTransactionFilter(
            status = "PAID"
        )
        val transaction = Transaction(
            id = 1,
            description = "Transação paga",
            amount = BigDecimal(100.0),
            downPayment = null,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PAID,
            categoryId = category,
            dueDate = LocalDate.of(2025, 8, 15),
            createdAt = Instant.now(),
            userId = user,
            installmentInfo = null,
            notes = null,
            recurrencePattern = null
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction), pageable, 1)
        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)
        assertEquals(1, result.totalElements)
        assertEquals("PAID", result.content[0].status)
    }

    @Test
    @DisplayName("Deve buscar transações do mês atual (currentMonth = true)")
    fun `should search current month transactions`() {
        val filter = SearchTransactionFilter(
            currentMonth = true
        )
        val currentDate = LocalDate.now()
        val transaction = Transaction(
            id = 1,
            description = "Transação do mês atual",
            amount = BigDecimal(100.0),
            downPayment = null,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PENDING,
            categoryId = category,
            dueDate = currentDate,
            createdAt = Instant.now(),
            userId = user,
            installmentInfo = null,
            notes = null,
            recurrencePattern = null
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction), pageable, 1)
        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)
        assertEquals(1, result.totalElements)
        assertEquals(currentDate.month, result.content[0].dueDate.month)
        assertEquals(currentDate.year, result.content[0].dueDate.year)
    }

    @Test
    @DisplayName("Deve buscar transações por nome da categoria")
    fun `should search transactions by category name`() {
        val filter = SearchTransactionFilter(
            categoryName = "Alimentação"
        )
        val transaction = Transaction(
            id = 1,
            description = "Compra no supermercado",
            amount = BigDecimal(100.0),
            downPayment = null,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PENDING,
            categoryId = Category(id = 1, name = "Alimentação", type = "EXPENSE"),
            dueDate = LocalDate.of(2025, 8, 15),
            createdAt = Instant.now(),
            userId = user,
            installmentInfo = null,
            notes = null,
            recurrencePattern = null
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction), pageable, 1)
        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)
        assertEquals(1, result.totalElements)
        assertEquals("Compra no supermercado", result.content[0].description)
    }

    @Test
    @DisplayName("Deve buscar transações com múltiplos filtros avançados")
    fun `should search transactions with multiple advanced filters`() {
        val installmentInfo = InstallmentInfo(
            totalInstallments = 3,
            currentInstallment = 1,
            installmentValue = BigDecimal(100.0)
        )
        val filter = SearchTransactionFilter(
            status = "PENDING",
            hasDownPayment = true,
            isInstallment = true,
            minAmount = BigDecimal(200.0),
            maxAmount = BigDecimal(400.0),
            startDate = LocalDate.of(2025, 8, 1),
            endDate = LocalDate.of(2025, 8, 31)
        )
        val transaction = Transaction(
            id = 1,
            description = "Transação complexa",
            amount = BigDecimal(300.0),
            downPayment = BigDecimal(50.0),
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PENDING,
            categoryId = category,
            dueDate = LocalDate.of(2025, 8, 15),
            createdAt = Instant.now(),
            userId = user,
            installmentInfo = installmentInfo,
            notes = null,
            recurrencePattern = null
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction), pageable, 1)
        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)
        assertEquals(1, result.totalElements)
        assertEquals("PENDING", result.content[0].status)
        assertEquals(BigDecimal(50.0), result.content[0].downPayment)
        assertEquals(3, result.content[0].installmentInfo?.totalInstallments)
    }
}
