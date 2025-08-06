package io.red.financesK.transaction.controller.request

import java.math.BigDecimal
import java.time.LocalDate

class CreateTransactionRequest(
    val description: String,
    val amount: BigDecimal,
    val downPayment: BigDecimal? = null,
    val type: String, // 'EXPENSE' or 'INCOME'
    val status: String? = "PENDING", // 'PENDING', 'PAID', 'OVERDUE'
    val categoryId: Int,
    val dueDate: LocalDate,
    val notes: String? = null,
    val recurrencePattern: String? = null,
    val totalInstallments: Int,
    val currentInstallment: Int? = null,
    val userId: Int,
    val accountId: Int? = null
)

