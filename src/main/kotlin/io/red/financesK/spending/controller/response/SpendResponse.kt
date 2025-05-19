package io.red.financesK.spending.controller.response

import java.math.BigDecimal

class SpendResponse(
    val id: Long?,
    val name: String,
    val description: String?,
    val amount: BigDecimal,
    val dueDate: String,
    val categoryName: String,
    val isDue: Boolean = false,
    val isPaid: Boolean = false,
    val isRecurring: Boolean = false,
    val status: String? = null
) {
}
