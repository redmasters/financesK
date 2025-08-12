package io.red.financesK.transaction.controller.response

import io.red.financesK.transaction.enums.TransactionType
import java.math.BigDecimal

data class TransactionStatsResponse(
    val totalTransactions: Long,
    val totalIncome: BigDecimal,
    val totalExpenses: BigDecimal,
    val balance: BigDecimal,
    val pendingTransactions: Long,
    val paidTransactions: Long,
    val averageTransactionAmount: BigDecimal,
    val transactionsByType: Map<TransactionType, BigDecimal>
)
