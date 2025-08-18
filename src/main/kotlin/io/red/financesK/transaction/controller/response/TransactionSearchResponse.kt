package io.red.financesK.transaction.controller.response

import org.springframework.data.domain.Page
import java.math.BigDecimal

data class TransactionSearchResponse(
    val transactions: Page<TransactionResponse>,
    val totalIncome: BigDecimal,
    val totalIncomeFormatted: String,
    val totalExpense: BigDecimal,
    val totalExpenseFormatted: String,
    val balance: BigDecimal,
    val balanceFormatted: String,
    val currency: String = "R$"
)
