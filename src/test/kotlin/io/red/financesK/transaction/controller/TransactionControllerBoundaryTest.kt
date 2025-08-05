package io.red.financesK.transaction.controller

import io.red.financesK.transaction.controller.request.CreateTransactionRequest
import io.red.financesK.transaction.controller.response.TransactionResponse
import io.red.financesK.transaction.service.create.CreateTransactionService
import io.red.financesK.transaction.service.search.SearchTransactionByIdService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.ResponseEntity
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.http.MediaType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.red.financesK.transaction.model.InstallmentInfo
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@ExtendWith(MockitoExtension::class)
class TransactionControllerBoundaryTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var createTransactionService: CreateTransactionService

    @Mock
    private lateinit var searchTransactionByIdService: SearchTransactionByIdService

    @InjectMocks
    private lateinit var transactionController: TransactionController

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
            startDate = LocalDate.of(2025, 8, 4),
            notes = null,
            recurrencePattern = null,
            1,
            userId = 1,
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
            startDate = LocalDate.of(2025, 8, 4),
            notes = null,
            recurrencePattern = null,
            1,
            userId = 1,
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
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @DisplayName("Deve retornar erro ao buscar transação com id negativo (edge)")
    fun `should return error when searching transaction with negative id`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/transactions/-1")
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

}
