package io.red.financesK.transaction.controller.response

import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import java.math.BigDecimal
import java.time.LocalDate

data class TransactionSummaryResponse(
    val id: Int,
    val description: String,
    val amount: BigDecimal,
    val type: TransactionType,
    val status: PaymentStatus,
    val categoryName: String,
    val dueDate: LocalDate,
    val hasInstallments: Boolean = false
)
