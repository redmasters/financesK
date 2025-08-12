package io.red.financesK.transaction.service.search

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.red.financesK.transaction.controller.response.AmountIncomeExpenseResponse
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.repository.TransactionRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class SearchTransactionServiceTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var searchTransactionService: SearchTransactionService

    @BeforeEach
    fun setUp() {
        transactionRepository = mockk()
        searchTransactionService = SearchTransactionService(transactionRepository)
    }

    @Test
    @DisplayName("Deve calcular balance de receitas e despesas com sucesso")
    fun `should calculate income expense balance successfully`() {
        // Given
        val userId = 1
        val status = PaymentStatus.PAID
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)
        val mockResponse = AmountIncomeExpenseResponse(
            totalIncome = BigDecimal("1000.00"),
            totalExpense = BigDecimal("500.00"),
            balance = BigDecimal("500.00")
        )

        every {
            transactionRepository.getIncomeExpenseBalance(userId, status, startDate, endDate)
        } returns mockResponse

        // When
        val result = searchTransactionService.getIncomeExpenseBalance(userId, status, startDate, endDate)

        // Then
        assertNotNull(result)
        assertEquals(BigDecimal("1000.00"), result.totalIncome)
        assertEquals(BigDecimal("500.00"), result.totalExpense)
        assertEquals(BigDecimal("500.00"), result.balance)
        assertEquals("R$", result.currency)

        verify { transactionRepository.getIncomeExpenseBalance(userId, status, startDate, endDate) }
    }

    @Test
    @DisplayName("Deve calcular balance com status nulo")
    fun `should calculate balance with null status`() {
        // Given
        val userId = 1
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)
        val mockResponse = AmountIncomeExpenseResponse(
            totalIncome = BigDecimal("750.00"),
            totalExpense = BigDecimal("250.00"),
            balance = BigDecimal("500.00")
        )

        every {
            transactionRepository.getIncomeExpenseBalance(userId, null, startDate, endDate)
        } returns mockResponse

        // When
        val result = searchTransactionService.getIncomeExpenseBalance(userId, null, startDate, endDate)

        // Then
        assertNotNull(result)
        assertEquals(BigDecimal("750.00"), result.totalIncome)
        assertEquals(BigDecimal("250.00"), result.totalExpense)
        assertEquals(BigDecimal("500.00"), result.balance)

        verify { transactionRepository.getIncomeExpenseBalance(userId, null, startDate, endDate) }
    }

    @Test
    @DisplayName("Deve somar valores por tipo INCOME corretamente")
    fun `should sum amounts by INCOME type correctly`() {
        // Given
        val userId = 1
        val type = "INCOME"
        val status = "PAID"
        val startDate = "2025-01-01"
        val endDate = "2025-01-31"
        val incomeAmount = 1500

        every {
            transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.INCOME, PaymentStatus.PAID,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)
            )
        } returns incomeAmount

        // When
        val result = searchTransactionService.sumAmountByUserIdAndTypeAndDateRange(
            userId, type, status, startDate, endDate
        )

        // Then
        assertNotNull(result)
        assertEquals(BigDecimal("15.00"), result.totalIncome) // ConvertMoneyUtils converte centavos para reais
        assertEquals(BigDecimal("0.00"), result.totalExpense)
        assertEquals(BigDecimal("15.00"), result.balance)

        verify {
            transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.INCOME, PaymentStatus.PAID,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)
            )
        }
    }

    @Test
    @DisplayName("Deve somar valores por tipo EXPENSE corretamente")
    fun `should sum amounts by EXPENSE type correctly`() {
        // Given
        val userId = 1
        val type = "EXPENSE"
        val status = "PAID"
        val startDate = "2025-01-01"
        val endDate = "2025-01-31"
        val expenseAmount = 800

        every {
            transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.EXPENSE, PaymentStatus.PAID,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)
            )
        } returns expenseAmount

        // When
        val result = searchTransactionService.sumAmountByUserIdAndTypeAndDateRange(
            userId, type, status, startDate, endDate
        )

        // Then
        assertNotNull(result)
        assertEquals(BigDecimal("0.00"), result.totalIncome)
        assertEquals(BigDecimal("8.00"), result.totalExpense) // ConvertMoneyUtils converte centavos para reais
        assertEquals(BigDecimal("-8.00"), result.balance)

        verify {
            transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.EXPENSE, PaymentStatus.PAID,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)
            )
        }
    }

    @Test
    @DisplayName("Deve lidar com valores nulos do repositório")
    fun `should handle null values from repository`() {
        // Given
        val userId = 1
        val type = "INCOME"
        val status = "PENDING"
        val startDate = "2025-01-01"
        val endDate = "2025-01-31"

        every {
            transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.INCOME, PaymentStatus.PENDING,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)
            )
        } returns null

        // When
        val result = searchTransactionService.sumAmountByUserIdAndTypeAndDateRange(
            userId, type, status, startDate, endDate
        )

        // Then
        assertNotNull(result)
        assertEquals(BigDecimal("0.00"), result.totalIncome)
        assertEquals(BigDecimal("0.00"), result.totalExpense)
        assertEquals(BigDecimal("0.00"), result.balance)

        verify {
            transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.INCOME, PaymentStatus.PENDING,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)
            )
        }
    }

    @Test
    @DisplayName("Deve calcular balance com zero receitas e despesas")
    fun `should calculate balance with zero income and expenses`() {
        // Given
        val userId = 1
        val status = PaymentStatus.PAID
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)
        val mockResponse = AmountIncomeExpenseResponse(
            totalIncome = BigDecimal("0.00"),
            totalExpense = BigDecimal("0.00"),
            balance = BigDecimal("0.00")
        )

        every {
            transactionRepository.getIncomeExpenseBalance(userId, status, startDate, endDate)
        } returns mockResponse

        // When
        val result = searchTransactionService.getIncomeExpenseBalance(userId, status, startDate, endDate)

        // Then
        assertNotNull(result)
        assertEquals(BigDecimal("0.00"), result.totalIncome)
        assertEquals(BigDecimal("0.00"), result.totalExpense)
        assertEquals(BigDecimal("0.00"), result.balance)

        verify { transactionRepository.getIncomeExpenseBalance(userId, status, startDate, endDate) }
    }

    @Test
    @DisplayName("Deve validar conversão de datas string para LocalDate")
    fun `should validate string to LocalDate conversion`() {
        // Given
        val userId = 1
        val type = "INCOME"
        val status = "PAID"
        val startDate = "2025-12-01"
        val endDate = "2025-12-31"
        val incomeAmount = 2000

        every {
            transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.INCOME, PaymentStatus.PAID,
                LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 31)
            )
        } returns incomeAmount

        // When
        val result = searchTransactionService.sumAmountByUserIdAndTypeAndDateRange(
            userId, type, status, startDate, endDate
        )

        // Then
        assertNotNull(result)
        assertEquals(BigDecimal("20.00"), result.totalIncome)

        verify {
            transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.INCOME, PaymentStatus.PAID,
                LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 31)
            )
        }
    }

    @Test
    @DisplayName("Deve validar conversão de enums string para tipos corretos")
    fun `should validate string to enum conversion`() {
        // Given
        val userId = 1
        val type = "EXPENSE"
        val status = "PENDING"
        val startDate = "2025-01-01"
        val endDate = "2025-01-31"
        val expenseAmount = 1200

        every {
            transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.EXPENSE, PaymentStatus.PENDING,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)
            )
        } returns expenseAmount

        // When
        val result = searchTransactionService.sumAmountByUserIdAndTypeAndDateRange(
            userId, type, status, startDate, endDate
        )

        // Then
        assertNotNull(result)
        assertEquals(BigDecimal("0.00"), result.totalIncome)
        assertEquals(BigDecimal("12.00"), result.totalExpense)
        assertEquals(BigDecimal("-12.00"), result.balance)

        verify {
            transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.EXPENSE, PaymentStatus.PENDING,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)
            )
        }
    }

    @Test
    @DisplayName("Deve calcular balance negativo quando despesas maiores que receitas")
    fun `should calculate negative balance when expenses greater than income`() {
        // Given
        val userId = 1
        val status = PaymentStatus.PAID
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)
        val mockResponse = AmountIncomeExpenseResponse(
            totalIncome = BigDecimal("300.00"),
            totalExpense = BigDecimal("800.00"),
            balance = BigDecimal("-500.00")
        )

        every {
            transactionRepository.getIncomeExpenseBalance(userId, status, startDate, endDate)
        } returns mockResponse

        // When
        val result = searchTransactionService.getIncomeExpenseBalance(userId, status, startDate, endDate)

        // Then
        assertNotNull(result)
        assertEquals(BigDecimal("300.00"), result.totalIncome)
        assertEquals(BigDecimal("800.00"), result.totalExpense)
        assertEquals(BigDecimal("-500.00"), result.balance)

        verify { transactionRepository.getIncomeExpenseBalance(userId, status, startDate, endDate) }
    }
}
