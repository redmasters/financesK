package io.red.financesK.account.balance.controller.response

class BalanceAccountResponse(
    val accountId: Int?,
    val balance: String? = null,
    val currency: String? = null,
    val userId: Int,
    val updatedAt: String? = null,
)
