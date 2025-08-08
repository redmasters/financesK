package io.red.financesK.transaction.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.red.financesK.transaction.controller.request.CreateTransactionRequest
import io.red.financesK.transaction.controller.request.UpdateTransactionRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MockitoExtension::class)
class TransactionControllerBoundaryTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper().apply {
        registerModule(com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
    }

    @Test
    @DisplayName("Deve retornar erro ao criar transação com valor zero (boundary)")
    fun `should return error when creating transaction with zero amount`() {
        val request = CreateTransactionRequest(
            description = "Compra",
            amount = BigDecimal.ZERO,
            type = "EXPENSE",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 4),
            notes = null,
            recurrencePattern = null,
            totalInstallments = 0,
            userId = 1,
            accountId = 1
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @DisplayName("Deve retornar erro ao criar transação com valor negativo (boundary)")
    fun `should return error when creating transaction with negative amount`() {
        val request = CreateTransactionRequest(
            description = "Compra",
            amount = BigDecimal("-10.50"),
            type = "EXPENSE",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 4),
            notes = null,
            recurrencePattern = null,
            totalInstallments = 0,
            userId = 1,
            accountId = 1
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @DisplayName("Deve criar transação com valor mínimo válido (boundary)")
    fun `should create transaction with minimum valid amount`() {
        val request = CreateTransactionRequest(
            description = "Compra mínima",
            amount = BigDecimal("0.01"),
            type = "EXPENSE",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 4),
            notes = null,
            recurrencePattern = null,
            totalInstallments = 0,
            userId = 1,
            accountId = 1
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @DisplayName("Deve criar transação parcelada com entrada (boundary)")
    fun `should create installment transaction with down payment`() {
        val request = CreateTransactionRequest(
            description = "Compra parcelada com entrada",
            amount = BigDecimal("1200.00"),
            downPayment = BigDecimal("200.00"),
            type = "EXPENSE",
            status = "PENDING",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 1),
            notes = "Compra em 12x com entrada",
            recurrencePattern = "MONTHLY",
            totalInstallments = 12,
            currentInstallment = 1,
            userId = 1,
            accountId = 1
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @DisplayName("Deve retornar erro ao buscar transação inexistente (boundary)")
    fun `should return error when searching non-existent transaction`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/transactions/99999")
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("Deve buscar transações com filtros de período (boundary)")
    fun `should search transactions with date range filters`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/transactions/search")
                .param("startDate", "2025-08-01")
                .param("endDate", "2025-08-31")
                .param("minAmount", "100.00")
                .param("maxAmount", "1000.00")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @DisplayName("Deve buscar transações do mês atual (boundary)")
    fun `should search current month transactions`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/transactions/search")
                .param("currentMonth", "true")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @DisplayName("Deve buscar transações com entrada (boundary)")
    fun `should search transactions with down payment`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/transactions/search")
                .param("hasDownPayment", "true")
                .param("isInstallment", "true")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @DisplayName("Deve atualizar transação com dados válidos (boundary)")
    fun `should update transaction with valid data`() {
        val request = UpdateTransactionRequest(
            description = "Descrição atualizada",
            amount = BigDecimal("250.00"),
            type = "INCOME",
            status = "PAID",
            categoryId = 2,
            dueDate = LocalDate.of(2025, 8, 10),
            notes = "Nota atualizada",
            recurrencePattern = null,
            totalInstallments = null,
            currentInstallment = null
        )

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/transactions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @DisplayName("Deve retornar erro ao atualizar transação inexistente (boundary)")
    fun `should return error when updating non-existent transaction`() {
        val request = UpdateTransactionRequest(
            description = "Descrição atualizada",
            amount = BigDecimal("250.00"),
            type = "INCOME",
            status = "PAID"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/transactions/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("Deve criar transação recorrente semanal (boundary)")
    fun `should create weekly recurring transaction`() {
        val request = CreateTransactionRequest(
            description = "Gasto semanal",
            amount = BigDecimal("75.00"),
            type = "EXPENSE",
            status = "PENDING",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 5),
            notes = "Gasto recorrente semanal",
            recurrencePattern = "WEEKLY",
            totalInstallments = 0,
            userId = 1,
            accountId = 1
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @DisplayName("Deve criar transação recorrente anual (boundary)")
    fun `should create yearly recurring transaction`() {
        val request = CreateTransactionRequest(
            description = "Seguro anual",
            amount = BigDecimal("1200.00"),
            type = "EXPENSE",
            status = "PENDING",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 1),
            notes = "Pagamento anual",
            recurrencePattern = "YEARLY",
            totalInstallments = 0,
            userId = 1,
            accountId = 1
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
}
