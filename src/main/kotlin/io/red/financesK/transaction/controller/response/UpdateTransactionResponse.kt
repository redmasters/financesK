package io.red.financesK.transaction.controller.response

import java.time.Instant

data class UpdateTransactionResponse(
    val id: Int,
    val message: String = "Transaction updated successfully",
    val updatedAt: Instant
)
