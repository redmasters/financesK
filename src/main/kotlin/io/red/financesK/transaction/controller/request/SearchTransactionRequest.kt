package io.red.financesK.transaction.controller.request

import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.RecurrencePattern
import io.red.financesK.transaction.enums.TransactionType
import java.math.BigDecimal
import java.time.LocalDate

data class SearchTransactionRequest(
    val description: String? = null,
    val minAmount: BigDecimal? = null,
    val maxAmount: BigDecimal? = null,
    val type: TransactionType? = null,
    val operationType: AccountOperationType? = null,
    val status: PaymentStatus? = null,
    val categoryId: Int? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val recurrencePattern: RecurrencePattern? = null,
    val userId: Int? = null,
    val accountId: Int? = null,
    val hasInstallments: Boolean? = null
)
