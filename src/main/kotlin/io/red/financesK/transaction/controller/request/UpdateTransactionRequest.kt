package io.red.financesK.transaction.controller.request

import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.RecurrencePattern
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.model.InstallmentInfo
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate

data class UpdateTransactionRequest(
    val description: String? = null,

    @field:Positive(message = "Amount must be positive")
    val amount: BigDecimal? = null,

    val downPayment: BigDecimal? = null,

    val type: TransactionType? = null,

    val operationType: AccountOperationType? = null,

    val status: PaymentStatus? = null,

    val categoryId: Int? = null,

    val dueDate: LocalDate? = null,

    val notes: String? = null,

    val recurrencePattern: RecurrencePattern? = null,

    val installmentInfo: InstallmentInfo? = null,

    val accountId: Int? = null
)
