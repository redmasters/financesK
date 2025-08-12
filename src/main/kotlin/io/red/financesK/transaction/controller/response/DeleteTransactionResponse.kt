package io.red.financesK.transaction.controller.response

data class DeleteTransactionResponse(
    val id: Int,
    val message: String = "Transaction deleted successfully"
)
