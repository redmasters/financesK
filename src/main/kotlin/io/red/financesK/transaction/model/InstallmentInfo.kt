package io.red.financesK.transaction.model

data class InstallmentInfo(
    val totalInstallments: Int? = null,
    val currentInstallment: Int? = null,
    val installmentValue: Int? = null,
)
