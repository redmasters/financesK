package io.red.financesK.account.controller.response

class UpdateAccountResponse(
    val accountId: Int?,
    val name: String,
    val description: String? = null,
    val balance: String? = null, // Using String to handle potential formatting issues
    val currency: String = "BRL", // Default currency set to BRL
    val userId: Int?,
    val updatedAt: String? = null // Using String to handle date formatting issues
)
