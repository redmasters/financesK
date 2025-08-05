package io.red.financesK.transaction.controller

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.extension.ExtendWith
import io.red.financesK.transaction.controller.request.CreateTransactionRequest
import io.red.financesK.transaction.controller.response.TransactionResponse
import io.red.financesK.transaction.service.create.CreateTransactionService
import io.red.financesK.transaction.service.search.SearchTransactionByIdService
import io.red.financesK.transaction.service.search.SearchTransactionService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class TransactionControllerTest {

    @Mock
    private lateinit var createTransactionService: CreateTransactionService

    @Mock
    private lateinit var searchTransactionService: SearchTransactionService

    @InjectMocks
    private lateinit var transactionController: TransactionController

    @Test
    @DisplayName("Deve criar transação com sucesso")
    fun `should create transaction successfully`() {
        val request = CreateTransactionRequest(
            description = "Compra",
            amount = BigDecimal(100.0),
            type = "EXPENSE",
            categoryId = 1,
            startDate = LocalDate.of(2025, 8, 4),
            notes = null,
            recurrencePattern = null,
            1,
            userId = 1,
        )
        Mockito.doNothing().`when`(createTransactionService).execute(request)
        assertDoesNotThrow { transactionController.create(request) }
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar transação com valor zero")
    fun `should throw exception when creating transaction with zero amount`() {
        val request = CreateTransactionRequest(
            description = "Compra",
            amount = BigDecimal(0.0),
            type = "EXPENSE",
            categoryId = 1,
            startDate = LocalDate.of(2025, 8, 4),
            notes = null,
            recurrencePattern = null,
            1,
            userId = 1,
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
            startDate = LocalDate.of(2025, 8, 4),
            notes = null,
            recurrencePattern = null,
            1,
            userId = 1,
        )
        Mockito.doThrow(io.red.financesK.global.exception.ValidationException("Transaction amount must be greater than zero"))
            .`when`(createTransactionService).execute(request)
        val exception = assertThrows<io.red.financesK.global.exception.ValidationException> {
            transactionController.create(request)
        }
        assertEquals("Transaction amount must be greater than zero", exception.message)
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar transação inexistente")
    fun `should throw exception when searching for non-existent transaction`() {
        val id = 999
        Mockito.`when`(searchTransactionService.searchById(id)).thenReturn(null)
        val response = transactionController.getById(id)
        assertEquals(404, response.statusCode.value())
    }

    @Test
    @DisplayName("Deve buscar transação por id com sucesso")
    fun `should get transaction by id successfully`() {
        val id = 3
        val responseMock = TransactionResponse(
            id = id,
            description = "Compra",
            amount = BigDecimal(100.0),
            type = "EXPENSE",
            categoryId = 1,
            transactionDate = LocalDate.of(2025, 8, 4),
            Instant.now(),
            userId = 1,
            installmentInfo = null,
            notes = null,
            recurrencePattern = null
        )
        Mockito.`when`(searchTransactionService.searchById(id)).thenReturn(responseMock)
        val response = transactionController.getById(id)
        assertEquals(200, response.statusCode.value())
        assertEquals(responseMock, response.body)
    }

    @Test
    @DisplayName("Deve criar transação parcelada com sucesso")
    fun `should create installment transaction successfully`() {

        val request = CreateTransactionRequest(
            description = "Compra parcelada",
            amount = BigDecimal(120.0),
            type = "EXPENSE",
            categoryId = 1,
            startDate = LocalDate.of(2025, 8, 4),
            notes = null,
            recurrencePattern = "MONTHLY",
            12,
            userId = 1,
        )
        Mockito.doNothing().`when`(createTransactionService).execute(request)
        assertDoesNotThrow { transactionController.create(request) }
    }
}
