package io.red.financesK.transaction.controller.request

import java.math.BigDecimal
import java.time.LocalDate

data class UpdateTransactionRequest(
    val description: String? = null,
    val amount: BigDecimal? = null,
    val downPayment: BigDecimal? = null,
    val type: String? = null, // 'EXPENSE' or 'INCOME'
    val categoryId: Int? = null,
    val dueDate: LocalDate? = null,
    val notes: String? = null,
    val recurrencePattern: String? = null,
    val totalInstallments: Int? = null,
    val currentInstallment: Int? = null,
    val status: String? = null // 'PENDING', 'PAID', 'OVERDUE'
)
