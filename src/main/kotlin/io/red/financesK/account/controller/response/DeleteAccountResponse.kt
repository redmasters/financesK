package io.red.financesK.account.controller.response

data class DeleteAccountResponse(
    val accountId: Int,
    val message: String = "Account deleted successfully"
)
