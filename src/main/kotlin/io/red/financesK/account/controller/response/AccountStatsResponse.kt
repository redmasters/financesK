package io.red.financesK.account.controller.response

import java.math.BigDecimal

data class AccountStatsResponse(
    val totalAccounts: Long,
    val totalBalance: BigDecimal,
    val averageBalance: BigDecimal,
    val accountsByCurrency: Map<String, Long>,
    val balancesByCurrency: Map<String, BigDecimal>,
    val activeAccounts: Long,
    val inactiveAccounts: Long
)
