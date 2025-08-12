package io.red.financesK.transaction.controller.response

import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.RecurrencePattern
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.model.InstallmentInfo
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class TransactionResponse(
    val id: Int,
    val description: String,
    val amount: BigDecimal,
    val downPayment: BigDecimal? = null,
    val type: TransactionType,
    val operationType: AccountOperationType,
    val status: PaymentStatus,
    val categoryId: Int,
    val categoryName: String? = null,
    val dueDate: LocalDate,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val notes: String? = null,
    val recurrencePattern: RecurrencePattern? = null,
    val installmentInfo: InstallmentInfo? = null,
    val userId: Int,
    val accountId: Int,
    val accountName: String? = null
)
