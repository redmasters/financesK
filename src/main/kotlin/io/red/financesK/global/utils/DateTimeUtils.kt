package io.red.financesK.global.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DateTimeUtils {
    companion object {
        private const val BRAZILIAN_TIMEZONE = "America/Sao_Paulo"
        private const val BRAZILIAN_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss"
        private const val BRAZILIAN_DATETIME_WITH_TIMEZONE_FORMAT = "dd/MM/yyyy HH:mm:ss z"

        fun formatToBrazilianDateTime(instant: Instant?): String {
            return instant?.atZone(ZoneId.of(BRAZILIAN_TIMEZONE))
                ?.format(DateTimeFormatter.ofPattern(BRAZILIAN_DATETIME_FORMAT))
                ?: "N/A"
        }

        fun formatToBrazilianDateTimeWithTimeZone(instant: Instant?): String {
            return instant?.atZone(ZoneId.of(BRAZILIAN_TIMEZONE))
                ?.format(DateTimeFormatter.ofPattern(BRAZILIAN_DATETIME_WITH_TIMEZONE_FORMAT))
                ?: "N/A"
        }

        // retorna a data no formato brasileiro dd/MM/yyyy do mes atual com o dia informado
        // Exemplo: se o dia for 15, retorna "15/10/2023" se o mes atual for outubro de 2023
        // ou "15/01/2024" se o mes atual for janeiro de 2024
        // Utilizado para formatar a data de fechamento da fatura do cartão de crédito
        // ou a data de vencimento da fatura do cartão de crédito
        // ou a data de fechamento da conta corrente
        fun formatToBrazilianDateByDayInput(day: Int): String {
            val currentDate = Instant.now().atZone(ZoneId.of(BRAZILIAN_TIMEZONE))
            val year = currentDate.year
            val month = currentDate.monthValue

            return String.format("%02d/%02d/%04d", day, month, year)

        }


    }
}
