package io.red.financesK.transaction.service.search

import io.mockk.every
import io.mockk.mockk
import io.red.financesK.transaction.model.Category
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.user.model.AppUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.Optional

class SearchTransactionByIdServiceTest {
    private val transactionRepository = mockk<TransactionRepository>()
    private val service = SearchTransactionByIdService(transactionRepository)

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
        every { transactionRepository.findById(1) } returns Optional.of(transaction)
        val response = service.execute(1)
        assertEquals(transaction.id, response?.id)
        assertEquals(transaction.description, response?.description)
        assertEquals(transaction.amount, response?.amount)
        assertEquals(transaction.type, response?.type)
        assertEquals(transaction.categoryId, response?.categoryId)
        assertEquals(transaction.transactionDate, response?.transactionDate)
        assertEquals(transaction.createdAt, response?.createdAt)
        assertEquals(transaction.notes, response?.notes)
        assertEquals(transaction.recurrencePattern, response?.recurrencePattern)
        assertEquals(transaction.installmentInfo, response?.installmentInfo)
        assertEquals(transaction.userId, response?.userId)
    }

    @Test
    @DisplayName("Deve retornar null quando não encontrar a transação pelo id")
    fun `should return null when transaction not found`() {
        every { transactionRepository.findById(99) } returns Optional.empty()
        val response = service.execute(99)
        assertNull(response)
    }
}

