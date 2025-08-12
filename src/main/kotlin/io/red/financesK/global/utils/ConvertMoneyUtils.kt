package io.red.financesK.global.utils

import java.math.BigDecimal

class ConvertMoneyUtils {
    companion object {
        fun convertToCents(amount: BigDecimal): Int {
            return amount.multiply(BigDecimal(100)).setScale(0).toInt()
        }

        fun convertToDecimal(cents: Int): BigDecimal {
            return BigDecimal(cents).divide(BigDecimal(100)).setScale(2)
        }
    }
}
