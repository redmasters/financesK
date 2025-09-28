package io.red.financesK.global.utils

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MoneyFormatterUtils {


    companion object {
        private val brazilianLocale = Locale.of("pt", "BR")
        private val decimalFormatSymbols = DecimalFormatSymbols(brazilianLocale)
        private val decimalFormat = DecimalFormat("#,##0.00", decimalFormatSymbols)
        private const val ZERO_REAIS = "R$ 0,00"

        fun formatToBrazilianCurrency(amount: BigDecimal?): String {
            return if (amount != null) {
                "R$ ${decimalFormat.format(amount)}"
            } else {
                ZERO_REAIS
            }
        }

        fun formatToBrazilianCurrency(amount: Number?): String {
            return if (amount != null) {
                formatToBrazilianCurrency(BigDecimal(amount.toString()))
            } else {
                ZERO_REAIS
            }
        }

        fun formatToBrazilianCurrency(amount: Int?): String {
            return if (amount != null) {
                formatToBrazilianCurrency(BigDecimal(amount))
            } else {
                ZERO_REAIS
            }
        }

        fun formatToBrazilianCurrency(amount: Double?): String {
            return if (amount != null) {
                formatToBrazilianCurrency(BigDecimal.valueOf(amount))
            } else {
                ZERO_REAIS
            }
        }
    }
}
