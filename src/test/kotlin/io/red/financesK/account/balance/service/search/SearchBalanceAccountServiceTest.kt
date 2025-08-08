package io.red.financesK.account.balance.service.search

import io.red.financesK.account.balance.controller.response.BalanceSummaryResponse
import io.red.financesK.account.balance.repository.AccountBalanceHistoryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

class SearchBalanceAccountServiceTest {

    private val accountBalanceHistoryRepository: AccountBalanceHistoryRepository = mock()
    private lateinit var service: SearchBalanceAccountService

    @BeforeEach
    fun setup() {
        service = SearchBalanceAccountService(accountBalanceHistoryRepository)
    }

    @Test
    @DisplayName("Deve retornar soma total dos saldos corretamente")
    fun `should return total balance sum correctly`() {
        // Given
        val userId = 1
        val startDate = "2025-08-01"
        val endDate = "2025-08-31"
        val accountIds = listOf(3, 4, 5)
        val expectedTotal = BigDecimal("2049.20")

        `when`(
            accountBalanceHistoryRepository.getTotalBalanceByUserAndDateRangeAndAccounts(
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                accountIds
            )
        ).thenReturn(expectedTotal)

        // When
        val result = service.getSumBalanceByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)

        // Then
        assertEquals(expectedTotal, result)
        verify(accountBalanceHistoryRepository).getTotalBalanceByUserAndDateRangeAndAccounts(
            userId,
            LocalDate.parse(startDate),
            LocalDate.parse(endDate),
            accountIds
        )
    }

    @Test
    @DisplayName("Deve retornar zero quando não há saldos para o período")
    fun `should return zero when no balances for period`() {
        // Given
        val userId = 1
        val startDate = "2025-08-01"
        val endDate = "2025-08-31"
        val accountIds = listOf(1, 2)
        val expectedTotal = BigDecimal.ZERO

        `when`(
            accountBalanceHistoryRepository.getTotalBalanceByUserAndDateRangeAndAccounts(
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                accountIds
            )
        ).thenReturn(expectedTotal)

        // When
        val result = service.getSumBalanceByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve funcionar com lista vazia de contas (boundary test)")
    fun `should work with empty account list - boundary test`() {
        // Given
        val userId = 1
        val startDate = "2025-08-01"
        val endDate = "2025-08-31"
        val accountIds = emptyList<Int>()
        val expectedTotal = BigDecimal.ZERO

        `when`(
            accountBalanceHistoryRepository.getTotalBalanceByUserAndDateRangeAndAccounts(
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                accountIds
            )
        ).thenReturn(expectedTotal)

        // When
        val result = service.getSumBalanceByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve funcionar com apenas uma conta (boundary test)")
    fun `should work with single account - boundary test`() {
        // Given
        val userId = 1
        val startDate = "2025-08-01"
        val endDate = "2025-08-31"
        val accountIds = listOf(5)
        val expectedTotal = BigDecimal("1500.75")

        `when`(
            accountBalanceHistoryRepository.getTotalBalanceByUserAndDateRangeAndAccounts(
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                accountIds
            )
        ).thenReturn(expectedTotal)

        // When
        val result = service.getSumBalanceByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve processar valores negativos corretamente")
    fun `should handle negative values correctly`() {
        // Given
        val userId = 1
        val startDate = "2025-08-01"
        val endDate = "2025-08-31"
        val accountIds = listOf(1, 2)
        val expectedTotal = BigDecimal("-250.50")

        `when`(
            accountBalanceHistoryRepository.getTotalBalanceByUserAndDateRangeAndAccounts(
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                accountIds
            )
        ).thenReturn(expectedTotal)

        // When
        val result = service.getSumBalanceByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve processar valores muito grandes corretamente")
    fun `should handle very large values correctly`() {
        // Given
        val userId = 1
        val startDate = "2025-08-01"
        val endDate = "2025-08-31"
        val accountIds = listOf(1, 2, 3)
        val expectedTotal = BigDecimal("999999999.99")

        `when`(
            accountBalanceHistoryRepository.getTotalBalanceByUserAndDateRangeAndAccounts(
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                accountIds
            )
        ).thenReturn(expectedTotal)

        // When
        val result = service.getSumBalanceByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve funcionar com período de um dia (boundary test)")
    fun `should work with single day period - boundary test`() {
        // Given
        val userId = 1
        val startDate = "2025-08-15"
        val endDate = "2025-08-15"
        val accountIds = listOf(1, 2)
        val expectedTotal = BigDecimal("500.00")

        `when`(
            accountBalanceHistoryRepository.getTotalBalanceByUserAndDateRangeAndAccounts(
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                accountIds
            )
        ).thenReturn(expectedTotal)

        // When
        val result = service.getSumBalanceByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve lançar exceção para formato de data inválido na data inicial")
    fun `should throw exception for invalid start date format`() {
        // Given
        val userId = 1
        val startDate = "invalid-date"
        val endDate = "2025-08-31"
        val accountIds = listOf(1, 2)

        // When & Then
        assertThrows<DateTimeParseException> {
            service.getSumBalanceByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)
        }
    }

    @Test
    @DisplayName("Deve lançar exceção para formato de data inválido na data final")
    fun `should throw exception for invalid end date format`() {
        // Given
        val userId = 1
        val startDate = "2025-08-01"
        val endDate = "invalid-date"
        val accountIds = listOf(1, 2)

        // When & Then
        assertThrows<DateTimeParseException> {
            service.getSumBalanceByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)
        }
    }

    @Test
    @DisplayName("Deve processar datas em formato ISO correto")
    fun `should process dates in correct ISO format`() {
        // Given
        val userId = 1
        val startDate = "2025-08-01"
        val endDate = "2025-08-31"
        val accountIds = listOf(1, 2)
        val expectedTotal = BigDecimal("1000.00")

        `when`(
            accountBalanceHistoryRepository.getTotalBalanceByUserAndDateRangeAndAccounts(
                userId,
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 31),
                accountIds
            )
        ).thenReturn(expectedTotal)

        // When
        val result = service.getSumBalanceByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)

        // Then
        assertEquals(expectedTotal, result)
        verify(accountBalanceHistoryRepository).getTotalBalanceByUserAndDateRangeAndAccounts(
            userId,
            LocalDate.of(2025, 8, 1),
            LocalDate.of(2025, 8, 31),
            accountIds
        )
    }

    @Test
    @DisplayName("Deve processar userId zero (boundary test)")
    fun `should handle userId zero - boundary test`() {
        // Given
        val userId = 0
        val startDate = "2025-08-01"
        val endDate = "2025-08-31"
        val accountIds = listOf(1)
        val expectedTotal = BigDecimal.ZERO

        `when`(
            accountBalanceHistoryRepository.getTotalBalanceByUserAndDateRangeAndAccounts(
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                accountIds
            )
        ).thenReturn(expectedTotal)

        // When
        val result = service.getSumBalanceByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve processar múltiplas contas com saldos diversos")
    fun `should process multiple accounts with various balances`() {
        // Given
        val userId = 1
        val startDate = "2025-08-01"
        val endDate = "2025-08-31"
        val accountIds = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val expectedTotal = BigDecimal("15000.50")

        `when`(
            accountBalanceHistoryRepository.getTotalBalanceByUserAndDateRangeAndAccounts(
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                accountIds
            )
        ).thenReturn(expectedTotal)

        // When
        val result = service.getSumBalanceByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve retornar soma total e data de última atualização corretamente")
    fun `should return total balance and last update date correctly`() {
        // Given
        val userId = 1
        val startDate = "2025-08-01"
        val endDate = "2025-08-31"
        val accountIds = listOf(3, 4, 5)
        val expectedSummary = BalanceSummaryResponse(
            totalBalance = BigDecimal("2049.20"),
            lastUpdated = Instant.parse("2025-08-30T14:30:00Z")
        )

        `when`(
            accountBalanceHistoryRepository.getTotalBalanceWithLastUpdateByUserAndDateRangeAndAccounts(
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                accountIds
            )
        ).thenReturn(expectedSummary)

        // When
        val result = service.getSumBalanceWithLastUpdateByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)

        // Then
        assertEquals(expectedSummary.totalBalance, result.totalBalance)
        assertEquals(expectedSummary.lastUpdated, result.lastUpdated)
        verify(accountBalanceHistoryRepository).getTotalBalanceWithLastUpdateByUserAndDateRangeAndAccounts(
            userId,
            LocalDate.parse(startDate),
            LocalDate.parse(endDate),
            accountIds
        )
    }

    @Test
    @DisplayName("Deve retornar zero e data nula quando não há histórico para o período")
    fun `should return zero and null date when no history for period`() {
        // Given
        val userId = 1
        val startDate = "2025-08-01"
        val endDate = "2025-08-31"
        val accountIds = listOf(1, 2)
        val expectedSummary = BalanceSummaryResponse(
            totalBalance = BigDecimal.ZERO,
            lastUpdated = null
        )

        `when`(
            accountBalanceHistoryRepository.getTotalBalanceWithLastUpdateByUserAndDateRangeAndAccounts(
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                accountIds
            )
        ).thenReturn(expectedSummary)

        // When
        val result = service.getSumBalanceWithLastUpdateByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)

        // Then
        assertEquals(BigDecimal.ZERO, result.totalBalance)
        assertEquals(null, result.lastUpdated)
    }

    @Test
    @DisplayName("Deve processar valores negativos com data de última atualização")
    fun `should handle negative values with last update date`() {
        // Given
        val userId = 1
        val startDate = "2025-08-01"
        val endDate = "2025-08-31"
        val accountIds = listOf(1, 2)
        val expectedSummary = BalanceSummaryResponse(
            totalBalance = BigDecimal("-250.50"),
            lastUpdated = Instant.parse("2025-08-25T10:15:30Z")
        )

        `when`(
            accountBalanceHistoryRepository.getTotalBalanceWithLastUpdateByUserAndDateRangeAndAccounts(
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                accountIds
            )
        ).thenReturn(expectedSummary)

        // When
        val result = service.getSumBalanceWithLastUpdateByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)

        // Then
        assertEquals(expectedSummary.totalBalance, result.totalBalance)
        assertEquals(expectedSummary.lastUpdated, result.lastUpdated)
    }

    @Test
    @DisplayName("Deve lançar exceção para data inválida no método com última atualização")
    fun `should throw exception for invalid date in method with last update`() {
        // Given
        val userId = 1
        val startDate = "invalid-date"
        val endDate = "2025-08-31"
        val accountIds = listOf(1, 2)

        // When & Then
        assertThrows<DateTimeParseException> {
            service.getSumBalanceWithLastUpdateByUserAndDateRangeAndAccounts(userId, startDate, endDate, accountIds)
        }
    }
}
