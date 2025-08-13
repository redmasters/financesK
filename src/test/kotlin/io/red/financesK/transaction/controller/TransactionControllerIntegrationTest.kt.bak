package io.red.financesK.transaction.controller

import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.service.search.SearchTransactionService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import io.red.financesK.transaction.controller.response.AmountIncomeExpenseResponse
import io.red.financesK.transaction.service.create.CreateTransactionService
import io.red.financesK.transaction.TransactionController
import java.math.BigDecimal
import java.time.LocalDate

@WebMvcTest(controllers = [TransactionController::class])
class TransactionControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var createTransactionService: CreateTransactionService

    @MockBean
    private lateinit var searchTransactionService: SearchTransactionService

    @Test
    @DisplayName("Deve retornar estatísticas de receitas e despesas via endpoint GET")
    fun `should return income expense balance via GET endpoint`() {
        // Given
        val userId = 1
        val status = PaymentStatus.PAID
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)

        val expectedResponse = AmountIncomeExpenseResponse(
            totalIncome = BigDecimal.valueOf(2500.0),
            totalExpense = BigDecimal.valueOf(1200.0),
            balance = BigDecimal.valueOf(1300.0)
        )

        `when`(searchTransactionService.getIncomeExpenseBalance(
            userId, status, startDate, endDate
        )).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/transactions/stats/income-expense-balance")
                .param("userId", userId.toString())
                .param("status", "PAID")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-01-31")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalIncome").value(2500.0))
            .andExpect(jsonPath("$.totalExpense").value(1200.0))
            .andExpect(jsonPath("$.balance").value(1300.0))
            .andExpect(jsonPath("$.currency").value("R$"))

        verify(searchTransactionService).getIncomeExpenseBalance(userId, status, startDate, endDate)
    }

    @Test
    @DisplayName("Deve retornar soma de valores por tipo via endpoint GET")
    fun `should return sum amounts by type via GET endpoint`() {
        // Given
        val userId = 1
        val type = "INCOME"
        val status = "PAID"
        val startDate = "2025-01-01"
        val endDate = "2025-01-31"

        val expectedResponse = AmountIncomeExpenseResponse(
            totalIncome = BigDecimal.valueOf(3000.0),
            totalExpense = BigDecimal.valueOf(0.0),
            balance = BigDecimal.valueOf(3000.0)
        )

        `when`(searchTransactionService.sumAmountByUserIdAndTypeAndDateRange(
            userId, type, status, startDate, endDate
        )).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/transactions/sumAmount")
                .param("userId", userId.toString())
                .param("type", type)
                .param("status", status)
                .param("startDate", startDate)
                .param("endDate", endDate)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalIncome").value(3000.0))
            .andExpect(jsonPath("$.totalExpense").value(0.0))
            .andExpect(jsonPath("$.balance").value(3000.0))
            .andExpect(jsonPath("$.currency").value("R$"))

        verify(searchTransactionService).sumAmountByUserIdAndTypeAndDateRange(userId, type, status, startDate, endDate)
    }

    @Test
    @DisplayName("Deve retornar erro 400 para parâmetros inválidos")
    fun `should return 400 for invalid parameters`() {
        // When & Then - Spring Boot automaticamente retorna 400 para erro de conversão de tipo
        mockMvc.perform(
            get("/api/v1/transactions/stats/income-expense-balance")
                .param("userId", "invalid")
                .param("status", "PAID")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-01-31")
        )
            .andExpect(status().is4xxClientError) // Aceita qualquer erro 4xx

        verifyNoInteractions(searchTransactionService)
    }

    @Test
    @DisplayName("Deve retornar saldo negativo quando despesas maiores que receitas")
    fun `should return negative balance when expenses greater than income`() {
        // Given
        val userId = 1
        val status = PaymentStatus.PAID
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)

        val expectedResponse = AmountIncomeExpenseResponse(
            totalIncome = BigDecimal.valueOf(1000.0),
            totalExpense = BigDecimal.valueOf(1500.0),
            balance = BigDecimal.valueOf(-500.0)
        )

        `when`(searchTransactionService.getIncomeExpenseBalance(
            userId, status, startDate, endDate
        )).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/transactions/stats/income-expense-balance")
                .param("userId", userId.toString())
                .param("status", "PAID")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-01-31")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalIncome").value(1000.0))
            .andExpect(jsonPath("$.totalExpense").value(1500.0))
            .andExpect(jsonPath("$.balance").value(-500.0))

        verify(searchTransactionService).getIncomeExpenseBalance(userId, status, startDate, endDate)
    }

    @Test
    @DisplayName("Deve retornar zero quando não há transações no período")
    fun `should return zero when no transactions in period`() {
        // Given
        val userId = 1
        val status = PaymentStatus.PAID
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)

        val expectedResponse = AmountIncomeExpenseResponse(
            totalIncome = BigDecimal.ZERO,
            totalExpense = BigDecimal.ZERO,
            balance = BigDecimal.ZERO
        )

        `when`(searchTransactionService.getIncomeExpenseBalance(
            userId, status, startDate, endDate
        )).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/transactions/stats/income-expense-balance")
                .param("userId", userId.toString())
                .param("status", "PAID")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-01-31")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalIncome").value(0.0))
            .andExpect(jsonPath("$.totalExpense").value(0.0))
            .andExpect(jsonPath("$.balance").value(0.0))

        verify(searchTransactionService).getIncomeExpenseBalance(userId, status, startDate, endDate)
    }

    @Test
    @DisplayName("Deve retornar balance com status opcional nulo")
    fun `should return balance with optional null status`() {
        // Given
        val userId = 1
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)

        val expectedResponse = AmountIncomeExpenseResponse(
            totalIncome = BigDecimal.valueOf(750.0),
            totalExpense = BigDecimal.valueOf(250.0),
            balance = BigDecimal.valueOf(500.0)
        )

        `when`(searchTransactionService.getIncomeExpenseBalance(
            userId, null, startDate, endDate
        )).thenReturn(expectedResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/transactions/stats/income-expense-balance")
                .param("userId", userId.toString())
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-01-31")
                // sem parâmetro status
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalIncome").value(750.0))
            .andExpect(jsonPath("$.totalExpense").value(250.0))
            .andExpect(jsonPath("$.balance").value(500.0))

        verify(searchTransactionService).getIncomeExpenseBalance(userId, null, startDate, endDate)
    }
}
