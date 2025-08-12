package io.red.financesK.account.controller.response

import java.math.BigDecimal

data class AccountSummaryResponse(
    val accountId: Int,
    val accountName: String,
    val accountCurrentBalance: BigDecimal,
    val accountCurrency: String,
    val isActive: Boolean = true
)
