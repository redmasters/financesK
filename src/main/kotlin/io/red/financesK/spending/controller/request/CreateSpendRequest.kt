package io.red.financesK.spending.controller.request

import java.math.BigDecimal

data class CreateSpendRequest(
    val name: String,
    val description: String? = null,
    val amount: BigDecimal,
    val dueDate: String,
    val categoryId: Long,
    val isPaid: Boolean = false,
    val isRecurring: Boolean = false,
    val status: String = "PENDING"
) {}

