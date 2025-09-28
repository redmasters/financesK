package io.red.financesK.transaction.controller.response

import java.time.Instant

data class CreateTransactionResponse(
    val id: Int,
    val message: String = "Transaction created successfully",
    val createdAt: Instant
)
