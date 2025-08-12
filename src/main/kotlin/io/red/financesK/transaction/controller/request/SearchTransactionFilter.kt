package io.red.financesK.transaction.controller.request

import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import java.time.LocalDate

data class SearchTransactionFilter(
    val userId: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val type: TransactionType? = null,
    val status: PaymentStatus? = null,
    val categoryId: Int? = null,
    val isRecurring: Boolean? = null,
    val hasInstallments: Boolean? = null,
    val description: String? = null,
    val minAmount: Int? = null,
    val maxAmount: Int? = null
)
