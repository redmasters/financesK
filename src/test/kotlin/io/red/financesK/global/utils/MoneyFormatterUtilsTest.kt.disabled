package io.red.financesK.global.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MoneyFormatterUtilsTest {

    @Test
    @DisplayName("Deve formatar valor positivo corretamente para padrão brasileiro")
    fun `should format positive value to brazilian currency format`() {
        val amount = BigDecimal("1234.56")
        val result = MoneyFormatterUtils.formatToBrazilianCurrency(amount)
        assertEquals("R$ 1.234,56", result)
    }

    @Test
    @DisplayName("Deve formatar valor negativo corretamente para padrão brasileiro")
    fun `should format negative value to brazilian currency format`() {
        val amount = BigDecimal("-1234.56")
        val result = MoneyFormatterUtils.formatToBrazilianCurrency(amount)
        assertEquals("R$ -1.234,56", result)
    }

    @Test
    @DisplayName("Deve formatar valor zero corretamente")
    fun `should format zero value correctly`() {
        val amount = BigDecimal.ZERO
        val result = MoneyFormatterUtils.formatToBrazilianCurrency(amount)
        assertEquals("R$ 0,00", result)
    }

    @Test
    @DisplayName("Deve formatar valor nulo como R$ 0,00")
    fun `should format null value as zero`() {
        val result = MoneyFormatterUtils.formatToBrazilianCurrency(null as BigDecimal?)
        assertEquals("R$ 0,00", result)
    }

    @Test
    @DisplayName("Deve formatar números grandes corretamente")
    fun `should format large numbers correctly`() {
        val amount = BigDecimal("1234567.89")
        val result = MoneyFormatterUtils.formatToBrazilianCurrency(amount)
        assertEquals("R$ 1.234.567,89", result)
    }

    @Test
    @DisplayName("Deve formatar valores inteiros corretamente")
    fun `should format integer values correctly`() {
        val amount = 1234
        val result = MoneyFormatterUtils.formatToBrazilianCurrency(amount)
        assertEquals("R$ 1.234,00", result)
    }

    @Test
    @DisplayName("Deve formatar valores double corretamente")
    fun `should format double values correctly`() {
        val amount = 1234.56
        val result = MoneyFormatterUtils.formatToBrazilianCurrency(amount)
        assertEquals("R$ 1.234,56", result)
    }
}
