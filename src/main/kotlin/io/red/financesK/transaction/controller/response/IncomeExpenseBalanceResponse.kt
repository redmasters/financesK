package io.red.financesK.transaction.controller.response

import java.math.BigDecimal

data class IncomeExpenseBalanceResponse(
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val balance: BigDecimal // income - expense
)
