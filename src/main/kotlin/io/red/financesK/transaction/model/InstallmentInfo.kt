package io.red.financesK.transaction.model

import java.math.BigDecimal

data class InstallmentInfo(
    val totalInstallments: Int? = null,
    val currentInstallment: Int? = null,
    val installmentValue: BigDecimal? = null,
)
