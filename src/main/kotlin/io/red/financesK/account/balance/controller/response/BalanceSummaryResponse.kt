package io.red.financesK.account.balance.controller.response

import java.math.BigDecimal
import java.time.Instant

data class BalanceSummaryResponse(
    val totalBalance: BigDecimal,
    val lastUpdated: Instant?
)
