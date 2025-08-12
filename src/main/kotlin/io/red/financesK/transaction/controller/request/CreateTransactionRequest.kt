package io.red.financesK.transaction.controller.request

import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate

data class CreateTransactionRequest(
    @field:NotBlank(message = "Description is required")
    val description: String,

    @field:NotNull(message = "Amount is required")
    @field:Positive(message = "Amount must be positive")
    val amount: BigDecimal,

    val downPayment: BigDecimal? = null,

    @field:NotNull(message = "Transaction type is required")
    val type: TransactionType,

    @field:NotNull(message = "Operation type is required")
    val operationType: AccountOperationType,

    val status: PaymentStatus? = PaymentStatus.PENDING,

    @field:NotNull(message = "Category ID is required")
    val categoryId: Int,

    @field:NotNull(message = "Due date is required")
    val dueDate: LocalDate,

    val notes: String? = null,

    val recurrencePattern: String? = null,
    val currentInstallment : Int? = null,
    val totalInstallments: Int? = null,

    @field:NotNull(message = "User ID is required")
    val userId: Int,

    @field:NotNull(message = "Account ID is required")
    val accountId: Int
)
