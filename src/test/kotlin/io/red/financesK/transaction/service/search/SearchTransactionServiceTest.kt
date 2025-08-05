package io.red.financesK.transaction.service.search

import io.mockk.every
import io.red.financesK.transaction.controller.request.SearchTransactionFilter
import io.red.financesK.transaction.controller.response.TransactionResponse
import io.red.financesK.transaction.model.Category
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.model.InstallmentInfo
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.transaction.repository.custom.TransactionCustomRepository
import io.red.financesK.user.model.AppUser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
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
            type = null,
            categoryId = category,
            transactionDate = LocalDate.of(2025, 8, 4),
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
    }

    @Test
    @DisplayName("Deve buscar transações por múltiplos campos")
    fun `should search transactions by multiple fields`() {
        val filter = SearchTransactionFilter(
            description = "Compra",
            amount = BigDecimal(100.0),
            type = "EXPENSE",
            categoryId = 1,
            startDate = LocalDate.of(2025, 8, 4),
            notes = "Nota",
            userId = 1
        )
        val transaction = Transaction(
            id = 1,
            description = "Compra",
            amount = BigDecimal(100.0),
            type = null,
            categoryId = category,
            transactionDate = LocalDate.of(2025, 8, 4),
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
            startDate = LocalDate.of(2025, 8, 4),
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
            startDate = LocalDate.of(2025, 8, 4),
            totalInstallments = 12,
            userId = 1,
            installmentInfo = installmentInfo
        )
        val transaction = Transaction(
            id = 2,
            description = "Compra parcelada",
            amount = BigDecimal(120.0),
            type = null,
            categoryId = category,
            transactionDate = LocalDate.of(2025, 8, 4),
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
    }


    @Test
    @DisplayName("Deve retornar a transação pelo id quando existir")
    fun `should return transaction response when found`() {
        val transaction = Transaction(
            id = 1,
            description = "Compra supermercado",
            amount = BigDecimal("150.00"),
            type = Transaction.TransactionType.EXPENSE,
            categoryId = Category(id = 1, name = "Supermercado", type = "EXPENSE"),
            transactionDate = LocalDate.of(2025, 8, 3),
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
        assertEquals(transaction.type.toString(), response?.type)
        assertEquals(transaction.categoryId.id, response?.categoryId)
        assertEquals(transaction.transactionDate, response?.transactionDate)
        assertEquals(transaction.createdAt, response?.createdAt)
        assertEquals(transaction.notes, response?.notes)
        assertEquals(transaction.recurrencePattern, response?.recurrencePattern)
        assertEquals(transaction.installmentInfo, response?.installmentInfo)
        assertEquals(transaction.userId.id, response?.userId)
    }

    @Test
    @DisplayName("Deve retornar null quando não encontrar a transação pelo id")
    fun `should return null when transaction not found`() {
        `when`(transactionRepository.findById(99)).thenReturn(Optional.empty())
        val response = searchTransactionService.searchById(99)
        assertNull(response)
    }}

