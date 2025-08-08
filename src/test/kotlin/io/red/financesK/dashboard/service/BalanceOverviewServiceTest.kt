package io.red.financesK.dashboard.service

import io.red.financesK.account.balance.controller.response.BalanceAccountResponse
import io.red.financesK.account.balance.service.search.SearchBalanceAccountService
import io.red.financesK.account.service.search.SearchAccountService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.math.BigDecimal

class BalanceOverviewServiceTest {

    private val searchAccountService: SearchAccountService = mock()
    private val searchBalanceAccountService: SearchBalanceAccountService = mock()
    private lateinit var service: BalanceOverviewService

    @BeforeEach
    fun setup() {
        service = BalanceOverviewService(searchAccountService, searchBalanceAccountService)
    }

    @Test
    @DisplayName("Deve calcular saldo total corretamente com múltiplas contas válidas")
    fun `should calculate total balance correctly with multiple valid accounts`() {
        // Given
        val accountIds = listOf(1, 2, 3)
        val balanceList = listOf(
            BalanceAccountResponse(1, "1000.50", "BRL", 1),
            BalanceAccountResponse(2, "2500.75", "BRL", 1),
            BalanceAccountResponse(3, "500.25", "BRL", 1)
        )
        val expectedTotal = BigDecimal("4001.50")

        `when`(searchAccountService.getBalanceByAccountList(accountIds)).thenReturn(balanceList)

        // When
        val result = service.getBalanceOverview(accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve retornar zero quando todas as contas têm saldo zero")
    fun `should return zero when all accounts have zero balance`() {
        // Given
        val accountIds = listOf(1, 2, 3)
        val balanceList = listOf(
            BalanceAccountResponse(1, "0.00", "BRL", 1),
            BalanceAccountResponse(2, "0.00", "BRL", 1),
            BalanceAccountResponse(3, "0.00", "BRL", 1)
        )
        val expectedTotal = BigDecimal("0.00")

        `when`(searchAccountService.getBalanceByAccountList(accountIds)).thenReturn(balanceList)

        // When
        val result = service.getBalanceOverview(accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve tratar valores de string inválidos como zero")
    fun `should treat invalid string values as zero`() {
        // Given
        val accountIds = listOf(1, 2, 3)
        val balanceList = listOf(
            BalanceAccountResponse(1, "1000.50", "BRL", 1),
            BalanceAccountResponse(2, "invalid_value", "BRL", 1),
            BalanceAccountResponse(3, "abc123", "BRL", 1)
        )
        val expectedTotal = BigDecimal("1000.50")

        `when`(searchAccountService.getBalanceByAccountList(accountIds)).thenReturn(balanceList)

        // When
        val result = service.getBalanceOverview(accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve retornar zero para lista vazia de contas")
    fun `should return zero for empty account list`() {
        // Given
        val accountIds = emptyList<Int>()
        val balanceList = emptyList<BalanceAccountResponse>()
        val expectedTotal = BigDecimal("0.00")

        `when`(searchAccountService.getBalanceByAccountList(accountIds)).thenReturn(balanceList)

        // When
        val result = service.getBalanceOverview(accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve calcular corretamente com valores decimais complexos")
    fun `should calculate correctly with complex decimal values`() {
        // Given
        val accountIds = listOf(1, 2, 3, 4)
        val balanceList = listOf(
            BalanceAccountResponse(1, "1234.567", "BRL", 1),
            BalanceAccountResponse(2, "9876.543", "BRL", 1),
            BalanceAccountResponse(3, "0.001", "BRL", 1),
            BalanceAccountResponse(4, "100.889", "BRL", 1)
        )
        val expectedTotal = BigDecimal("11212.00") // Arredondado para 2 casas decimais

        `when`(searchAccountService.getBalanceByAccountList(accountIds)).thenReturn(balanceList)

        // When
        val result = service.getBalanceOverview(accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve tratar valores negativos corretamente")
    fun `should handle negative values correctly`() {
        // Given
        val accountIds = listOf(1, 2, 3)
        val balanceList = listOf(
            BalanceAccountResponse(1, "1000.00", "BRL", 1),
            BalanceAccountResponse(2, "-500.00", "BRL", 1),
            BalanceAccountResponse(3, "250.00", "BRL", 1)
        )
        val expectedTotal = BigDecimal("750.00")

        `when`(searchAccountService.getBalanceByAccountList(accountIds)).thenReturn(balanceList)

        // When
        val result = service.getBalanceOverview(accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve tratar balance nulo como zero")
    fun `should treat null balance as zero`() {
        // Given
        val accountIds = listOf(1, 2, 3, 4)
        val balanceList = listOf(
            BalanceAccountResponse(1, "1000.00", "BRL", 1),
            BalanceAccountResponse(2, null, "BRL", 1),
            BalanceAccountResponse(3, "", "BRL", 1),
            BalanceAccountResponse(4, "500.00", "BRL", 1)
        )
        val expectedTotal = BigDecimal("1500.00")

        `when`(searchAccountService.getBalanceByAccountList(accountIds)).thenReturn(balanceList)

        // When
        val result = service.getBalanceOverview(accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve funcionar com apenas uma conta (boundary test)")
    fun `should work with single account - boundary test`() {
        // Given
        val accountIds = listOf(1)
        val balanceList = listOf(BalanceAccountResponse(1, "999.99", "BRL", 1))
        val expectedTotal = BigDecimal("999.99")

        `when`(searchAccountService.getBalanceByAccountList(accountIds)).thenReturn(balanceList)

        // When
        val result = service.getBalanceOverview(accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve tratar valores muito grandes corretamente")
    fun `should handle very large values correctly`() {
        // Given
        val accountIds = listOf(1, 2)
        val balanceList = listOf(
            BalanceAccountResponse(1, "999999999.99", "BRL", 1),
            BalanceAccountResponse(2, "1.01", "BRL", 1)
        )
        val expectedTotal = BigDecimal("1000000001.00")

        `when`(searchAccountService.getBalanceByAccountList(accountIds)).thenReturn(balanceList)

        // When
        val result = service.getBalanceOverview(accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }

    @Test
    @DisplayName("Deve garantir que o resultado sempre tenha 2 casas decimais")
    fun `should ensure result always has 2 decimal places`() {
        // Given
        val accountIds = listOf(1, 2)
        val balanceList = listOf(
            BalanceAccountResponse(1, "100", "BRL", 1),
            BalanceAccountResponse(2, "50.5", "BRL", 1)
        )
        val expectedTotal = BigDecimal("150.50")

        `when`(searchAccountService.getBalanceByAccountList(accountIds)).thenReturn(balanceList)

        // When
        val result = service.getBalanceOverview(accountIds)

        // Then
        assertEquals(expectedTotal, result)
        assertEquals(2, result.scale()) // Verifica se tem exatamente 2 casas decimais
    }

    @Test
    @DisplayName("Deve tratar formatação de número com vírgula (formato brasileiro)")
    fun `should handle comma decimal formatting - Brazilian format edge case`() {
        // Given
        val accountIds = listOf(1, 2)
        val balanceList = listOf(
            BalanceAccountResponse(1, "1,000.50", "BRL", 1), // This might be treated as invalid by toBigDecimalOrNull()
            BalanceAccountResponse(2, "500.00", "BRL", 1)
        )
        val expectedTotal = BigDecimal("500.00") // Only valid value should be counted

        `when`(searchAccountService.getBalanceByAccountList(accountIds)).thenReturn(balanceList)

        // When
        val result = service.getBalanceOverview(accountIds)

        // Then
        assertEquals(expectedTotal, result)
    }
}
