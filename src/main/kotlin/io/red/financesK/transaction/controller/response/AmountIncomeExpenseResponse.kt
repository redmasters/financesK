package io.red.financesK.transaction.controller.response

import io.red.financesK.global.utils.ConvertMoneyUtils
import io.red.financesK.global.utils.MoneyFormatterUtils
import java.math.BigDecimal

data class AmountIncomeExpenseResponse(
    val totalIncome: BigDecimal,
    val totalIncomeFormatted: String,
    val totalExpense: BigDecimal,
    val totalExpenseFormatted: String,
    val balance: BigDecimal,
    val balanceFormatted: String,
    val currency: String = "R$"
) {
    constructor(totalIncome: Number, totalExpense: Number, balance: Number) : this(
        totalIncome = ConvertMoneyUtils.convertToDecimal(totalIncome.toInt()),
        totalIncomeFormatted = MoneyFormatterUtils.formatToBrazilianCurrency(ConvertMoneyUtils.convertToDecimal(totalIncome.toInt())),
        totalExpense = ConvertMoneyUtils.convertToDecimal(totalExpense.toInt()),
        totalExpenseFormatted = MoneyFormatterUtils.formatToBrazilianCurrency(ConvertMoneyUtils.convertToDecimal(totalExpense.toInt())),
        balance = ConvertMoneyUtils.convertToDecimal(balance.toInt()),
        balanceFormatted = MoneyFormatterUtils.formatToBrazilianCurrency(ConvertMoneyUtils.convertToDecimal(balance.toInt()))
    )

    constructor(
        totalIncome: BigDecimal,
        totalExpense: BigDecimal,
        balance: BigDecimal
    ) : this(
        totalIncome = totalIncome,
        totalIncomeFormatted = MoneyFormatterUtils.formatToBrazilianCurrency(totalIncome),
        totalExpense = totalExpense,
        totalExpenseFormatted = MoneyFormatterUtils.formatToBrazilianCurrency(totalExpense),
        balance = balance,
        balanceFormatted = MoneyFormatterUtils.formatToBrazilianCurrency(balance)
    )
}
