package io.red.financesK.transaction.controller.response

import io.red.financesK.global.utils.ConvertMoneyUtils
import java.math.BigDecimal

data class AmountIncomeExpenseResponse(
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val balance: BigDecimal,
    val currency: String = "R$"
) {
    constructor(totalIncome: Number, totalExpense: Number, balance: Number) : this(
        totalIncome = ConvertMoneyUtils.convertToDecimal(totalIncome.toInt()),
        totalExpense = ConvertMoneyUtils.convertToDecimal(totalExpense.toInt()),
        balance = ConvertMoneyUtils.convertToDecimal(balance.toInt())
    )
}
