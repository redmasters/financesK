package io.red.financesK.transaction.controller.request

import java.math.BigDecimal
import java.time.LocalDate

// Request DTO for creating a transaction
class CreateTransactionRequest(
    val description: String,
    val amount: BigDecimal,
    val type: String, // 'EXPENSE' or 'INCOME'
    val categoryId: Int,
    val startDate: LocalDate,
    val notes: String? = null,
    val recurrencePattern: String? = null,
    val totalInstallments: Int,
    val userId: Int
)

