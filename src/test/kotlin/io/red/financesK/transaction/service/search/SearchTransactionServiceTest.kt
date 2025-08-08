package io.red.financesK.transaction.service.search

import io.red.financesK.account.model.Account
import io.red.financesK.transaction.controller.request.SearchTransactionFilter
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.model.Category
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.model.InstallmentInfo
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.transaction.repository.custom.TransactionCustomRepository
import io.red.financesK.user.model.AppUser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
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

@ExtendWith(MockitoExtension::class)
class SearchTransactionServiceTest {

    @Mock
    private lateinit var transactionCustomRepository: TransactionCustomRepository
    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @InjectMocks
    private lateinit var searchTransactionService: SearchTransactionService

    private lateinit var user: AppUser
    private lateinit var category: Category
    private lateinit var account: Account
    private lateinit var transaction: Transaction

    @BeforeEach
    fun setup() {
        user = AppUser(1, "testUser", "test@user", "hash", Instant.now())
        category = Category(id = 1, name = "Alimentação", type = "EXPENSE")
        account = Account(
            accountId = 1,
            accountName = "Conta Corrente",
            accountDescription = "Conta principal",
            accountInitialBalance = BigDecimal("1000.00"),
            accountCurrency = "BRL",
            userId = user
        )
        transaction = Transaction(
            id = 1,
            description = "Compra de supermercado",
            amount = BigDecimal("150.00"),
            downPayment = null,
            type = TransactionType.EXPENSE,
            status = PaymentStatus.PENDING,
            categoryId = category,
            dueDate = LocalDate.of(2025, 8, 3),
            createdAt = Instant.now(),
            notes = "Compra do mês",
            recurrencePattern = null,
            installmentInfo = null,
            userId = user,
            accountId = account
        )
    }

    @Test
    @DisplayName("Deve buscar transações por descrição")
    fun `should search transactions by description`() {
        val filter = SearchTransactionFilter(description = "Compra")
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction))

        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)

        assertEquals(1, result.totalElements)
        assertEquals("Compra de supermercado", result.content[0].description)
        assertEquals(BigDecimal("150.00"), result.content[0].amount)
    }

    @Test
    @DisplayName("Deve buscar transação por ID com sucesso")
    fun `should search transaction by id successfully`() {
        `when`(transactionRepository.findById(1)).thenReturn(Optional.of(transaction))

        val result = searchTransactionService.searchById(1)

        assertNotNull(result)
        assertEquals(1, result!!.id)
        assertEquals("Compra de supermercado", result.description)
        assertEquals(BigDecimal("150.00"), result.amount)
        assertEquals("EXPENSE", result.type)
    }

    @Test
    @DisplayName("Deve retornar null quando transação não for encontrada por ID")
    fun `should return null when transaction not found by id`() {
        `when`(transactionRepository.findById(999)).thenReturn(Optional.empty())

        val result = searchTransactionService.searchById(999)

        assertNull(result)
    }

    @Test
    @DisplayName("Deve buscar transações com filtros de valor")
    fun `should search transactions with amount filters`() {
        val filter = SearchTransactionFilter(
            minAmount = BigDecimal("100.00"),
            maxAmount = BigDecimal("200.00")
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction))

        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)

        assertEquals(1, result.totalElements)
        assertTrue(result.content[0].amount >= BigDecimal("100.00"))
        assertTrue(result.content[0].amount <= BigDecimal("200.00"))
    }

    @Test
    @DisplayName("Deve buscar transações com entrada (downPayment)")
    fun `should search transactions with down payment`() {
        val transactionWithDownPayment = transaction.copy(
            downPayment = BigDecimal("50.00"),
            installmentInfo = InstallmentInfo(
                totalInstallments = 3,
                currentInstallment = 1,
                installmentValue = BigDecimal("50.00")
            )
        )
        val filter = SearchTransactionFilter(hasDownPayment = true)
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transactionWithDownPayment))

        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)

        assertEquals(1, result.totalElements)
        assertEquals(BigDecimal("50.00"), result.content[0].downPayment)
        assertNotNull(result.content[0].installmentInfo)
    }

    @Test
    @DisplayName("Deve buscar transações parceladas")
    fun `should search installment transactions`() {
        val installmentTransaction = transaction.copy(
            installmentInfo = InstallmentInfo(
                totalInstallments = 12,
                currentInstallment = 1,
                installmentValue = BigDecimal("12.50")
            )
        )
        val filter = SearchTransactionFilter(isInstallment = true)
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(installmentTransaction))

        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)

        assertEquals(1, result.totalElements)
        assertNotNull(result.content[0].installmentInfo)
        assertEquals(12, result.content[0].installmentInfo?.totalInstallments)
    }

    @Test
    @DisplayName("Deve buscar transações por status")
    fun `should search transactions by status`() {
        val paidTransaction = transaction.copy(status = PaymentStatus.PAID)
        val filter = SearchTransactionFilter(status = "PAID")
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(paidTransaction))

        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)

        assertEquals(1, result.totalElements)
        assertEquals("PAID", result.content[0].status)
    }

    @Test
    @DisplayName("Deve buscar transações por tipo")
    fun `should search transactions by type`() {
        val incomeTransaction = transaction.copy(type = TransactionType.INCOME)
        val filter = SearchTransactionFilter(type = "INCOME")
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(incomeTransaction))

        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)

        assertEquals(1, result.totalElements)
        assertEquals("INCOME", result.content[0].type)
    }

    @Test
    @DisplayName("Deve buscar transações por período de datas")
    fun `should search transactions by date range`() {
        val filter = SearchTransactionFilter(
            startDate = LocalDate.of(2025, 8, 1),
            endDate = LocalDate.of(2025, 8, 31)
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction))

        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)

        assertEquals(1, result.totalElements)
        assertTrue(result.content[0].dueDate.isAfter(LocalDate.of(2025, 7, 31)))
        assertTrue(result.content[0].dueDate.isBefore(LocalDate.of(2025, 9, 1)))
    }

    @Test
    @DisplayName("Deve buscar transações do mês atual")
    fun `should search current month transactions`() {
        val filter = SearchTransactionFilter(currentMonth = true)
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction))

        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)

        assertEquals(1, result.totalElements)
        assertEquals(LocalDate.of(2025, 8, 3), result.content[0].dueDate)
    }

    @Test
    @DisplayName("Deve buscar transações por categoria")
    fun `should search transactions by category`() {
        val filter = SearchTransactionFilter(
            categoryId = 1,
            categoryName = "Alimentação"
        )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(transaction))

        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)

        assertEquals(1, result.totalElements)
        assertEquals(1, result.content[0].categoryId)
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhuma transação for encontrada")
    fun `should return empty list when no transactions found`() {
        val filter = SearchTransactionFilter(description = "NãoExiste")
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl<Transaction>(emptyList())

        `when`(transactionCustomRepository.findByDynamicFilter(filter, pageable)).thenReturn(page)

        val result = searchTransactionService.execute(filter, pageable)

        assertEquals(0, result.totalElements)
        assertTrue(result.content.isEmpty())
    }

    @Test
    @DisplayName("Deve buscar transações por vários filtros avançados")
    fun `should search transactions by multiple advanced filters`() {
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
