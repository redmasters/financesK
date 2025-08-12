package io.red.financesK.account.controller.response

import java.math.BigDecimal
import java.time.Instant

data class AccountResponse(
    val accountId: Int,
    val accountName: String,
    val accountDescription: String? = null,
    val accountCurrentBalance: BigDecimal,
    val accountCurrency: String,
    val userId: Int,
    val userName: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)
