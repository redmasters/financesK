package io.red.financesK.account.controller.request

import java.math.BigDecimal

data class SearchAccountRequest(
    val accountName: String? = null,
    val accountDescription: String? = null,
    val minBalance: BigDecimal? = null,
    val maxBalance: BigDecimal? = null,
    val accountCurrency: String? = null,
    val userId: Int? = null
)
