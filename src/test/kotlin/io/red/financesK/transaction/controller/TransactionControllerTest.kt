package io.red.financesK.transaction.controller

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.extension.ExtendWith
import io.red.financesK.transaction.controller.request.CreateTransactionRequest
import io.red.financesK.transaction.controller.request.SearchTransactionFilter
import io.red.financesK.transaction.controller.request.UpdateTransactionRequest
import io.red.financesK.transaction.controller.response.TransactionResponse
import io.red.financesK.transaction.model.InstallmentInfo
import io.red.financesK.transaction.service.create.CreateTransactionService
import io.red.financesK.transaction.service.search.SearchTransactionService
import io.red.financesK.transaction.service.update.UpdateTransactionService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class TransactionControllerTest {

    @Mock
    private lateinit var createTransactionService: CreateTransactionService

    @Mock
    private lateinit var searchTransactionService: SearchTransactionService

    @Mock
    private lateinit var updateTransactionService: UpdateTransactionService

    @InjectMocks
    private lateinit var transactionController: TransactionController

    @Test
    @DisplayName("Deve criar transação com sucesso")
    fun `should create transaction successfully`() {
        val request = CreateTransactionRequest(
            description = "Compra",
            amount = BigDecimal(100.0),
            type = "EXPENSE",
            status = "PENDING",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 4),
            notes = null,
            recurrencePattern = null,
            totalInstallments = 0,
            userId = 1,
            accountId = 1
        )

        Mockito.doNothing().`when`(createTransactionService).execute(request)

        val response = transactionController.create(request)

        assertEquals(HttpStatus.OK, response.statusCode)
        Mockito.verify(createTransactionService).execute(request)
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar transação com valor zero")
    fun `should throw exception when creating transaction with zero amount`() {
        val request = CreateTransactionRequest(
            description = "Compra",
            amount = BigDecimal(0.0),
            type = "EXPENSE",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 4),
            notes = null,
            recurrencePattern = null,
            totalInstallments = 1,
            userId = 1
        )
        Mockito.doThrow(io.red.financesK.global.exception.ValidationException("Transaction amount must be greater than zero"))
            .`when`(createTransactionService).execute(request)
        val exception = assertThrows<io.red.financesK.global.exception.ValidationException> {
            transactionController.create(request)
        }
        assertEquals("Transaction amount must be greater than zero", exception.message)
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar transação com valor negativo")
    fun `should throw exception when creating transaction with negative amount`() {
        val request = CreateTransactionRequest(
            description = "Compra",
            amount = BigDecimal(-10.0),
            type = "EXPENSE",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 4),
            notes = null,
            recurrencePattern = null,
            totalInstallments = 1,
            userId = 1
        )
        Mockito.doThrow(io.red.financesK.global.exception.ValidationException("Transaction amount must be greater than zero"))
            .`when`(createTransactionService).execute(request)
        val exception = assertThrows<io.red.financesK.global.exception.ValidationException> {
            transactionController.create(request)
        }
        assertEquals("Transaction amount must be greater than zero", exception.message)
    }

    @Test
    @DisplayName("Deve buscar transação por ID com sucesso")
    fun `should get transaction by id successfully`() {
        val transactionId = 1
        val expectedResponse = TransactionResponse(
            id = transactionId,
            description = "Compra de supermercado",
            amount = BigDecimal("150.00"),
            type = "EXPENSE",
            status = "PENDING",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 4),
            createdAt = Instant.now(),
            notes = "Compra do mês",
            recurrencePattern = null,
            installmentInfo = null,
            userId = 1
        )

        Mockito.`when`(searchTransactionService.searchById(transactionId)).thenReturn(expectedResponse)

        val response = transactionController.getById(transactionId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse, response.body)
        Mockito.verify(searchTransactionService).searchById(transactionId)
    }

    @Test
    @DisplayName("Deve retornar 404 quando transação não for encontrada")
    fun `should return 404 when transaction not found`() {
        val transactionId = 999

        Mockito.`when`(searchTransactionService.searchById(transactionId)).thenReturn(null)

        val response = transactionController.getById(transactionId)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Mockito.verify(searchTransactionService).searchById(transactionId)
    }

    @Test
    @DisplayName("Deve buscar transações com filtros com sucesso")
    fun `should search transactions with filters successfully`() {
        val transactions = listOf(
            TransactionResponse(
                id = 1,
                description = "Compra 1",
                amount = BigDecimal("100.00"),
                type = "EXPENSE",
                status = "PENDING",
                categoryId = 1,
                dueDate = LocalDate.of(2025, 8, 4),
                createdAt = Instant.now(),
                notes = null,
                recurrencePattern = null,
                installmentInfo = null,
                userId = 1
            )
        )
        val page = PageImpl(transactions, PageRequest.of(0, 10), 1)

        Mockito.`when`(searchTransactionService.execute(Mockito.any(SearchTransactionFilter::class.java), Mockito.any())).thenReturn(page)

        val response = transactionController.search(
            description = "Compra",
            amount = BigDecimal("100.00"),
            downPayment = null,
            type = "EXPENSE",
            categoryId = 1,
            status = "PENDING",
            startDate = null,
            endDate = null,
            dueDate = null,
            notes = null,
            recurrencePattern = null,
            totalInstallments = null,
            userId = 1,
            hasDownPayment = null,
            isInstallment = null,
            categoryName = null,
            minAmount = null,
            maxAmount = null,
            currentMonth = null,
            currentWeek = null,
            currentYear = null,
            page = 0,
            size = 10
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(1, response.body!!.content.size)
    }

    @Test
    @DisplayName("Deve atualizar transação com sucesso")
    fun `should update transaction successfully`() {
        val transactionId = 1
        val request = UpdateTransactionRequest(
            description = "Compra atualizada",
            amount = BigDecimal("200.00"),
            type = "EXPENSE",
            status = "PAID",
            categoryId = 2,
            dueDate = LocalDate.of(2025, 8, 5),
            notes = "Compra atualizada",
            recurrencePattern = null,
            totalInstallments = null,
            currentInstallment = null
        )

        val mockResponse = TransactionResponse(
            id = transactionId,
            description = "Compra atualizada",
            amount = BigDecimal("200.00"),
            type = "EXPENSE",
            status = "PAID",
            categoryId = 2,
            dueDate = LocalDate.of(2025, 8, 5),
            createdAt = Instant.now(),
            notes = "Compra atualizada",
            recurrencePattern = null,
            installmentInfo = null,
            userId = 1
        )

        Mockito.`when`(updateTransactionService.execute(transactionId, request)).thenReturn(mockResponse)

        val response = transactionController.update(transactionId, request)

        assertEquals(HttpStatus.OK, response.statusCode)
        Mockito.verify(updateTransactionService).execute(transactionId, request)
    }
}
