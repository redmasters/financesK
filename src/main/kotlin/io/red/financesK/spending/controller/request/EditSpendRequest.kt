package io.red.financesK.spending.controller.request

import java.math.BigDecimal

class EditSpendRequest(
    val name: String,
    val amount: BigDecimal,
    val dueDate: String,
    val categoryId: Long,
    val description: String? = null,
    val isPaid: Boolean,
    val isRecurring: Boolean,
    val status : String
) {
}
