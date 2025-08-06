package io.red.financesK.transaction.controller.response

import io.red.financesK.transaction.model.InstallmentInfo
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class TransactionResponse(
    val id: Int,
    val description: String,
    val amount: BigDecimal,
    val downPayment: BigDecimal? = null,
    val type: String,
    val status: String?,
    val categoryId: Int,
    val dueDate: LocalDate,
    val createdAt: Instant?,
    val updatedAt: Instant? = null,
    val notes: String?,
    val recurrencePattern: String?,
    val installmentInfo: InstallmentInfo? = null,
    val userId: Int
)
