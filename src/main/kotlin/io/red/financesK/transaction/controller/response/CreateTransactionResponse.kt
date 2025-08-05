package io.red.financesK.transaction.controller.response

import io.red.financesK.transaction.model.InstallmentInfo
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Instant

class CreateTransactionResponse(
    val id: Int,
    val description: String,
    val amount: BigDecimal,
    val type: String,
    val categoryId: Int,
    val transactionDate: LocalDate,
    val createdAt: Instant,
    val notes: String?,
    val recurrencePattern: String?,
    val installmentInfo: InstallmentInfo? = null,
    val userId: Int
)

