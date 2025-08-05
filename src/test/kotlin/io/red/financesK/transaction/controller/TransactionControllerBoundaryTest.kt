package io.red.financesK.transaction.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.red.financesK.transaction.controller.request.CreateTransactionRequest
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
@ActiveProfiles("dev")
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
            amount = BigDecimal(0.0),
            type = "EXPENSE",
            categoryId = 1,
            dueDate = LocalDate.of(2025, 8, 4),
            notes = null,
            recurrencePattern = null,
            totalInstallments = 1,
            userId = 1
        )
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @DisplayName("Deve retornar erro ao criar transação com valor negativo (edge)")
    fun `should return error when creating transaction with negative amount`() {
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
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @DisplayName("Deve retornar erro ao buscar transação com id zero (boundary)")
    fun `should return error when searching transaction with id zero`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/transactions/0")
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @DisplayName("Deve retornar erro ao buscar transação com id negativo (edge)")
    fun `should return error when searching transaction with negative id`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/transactions/-1")
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

}
