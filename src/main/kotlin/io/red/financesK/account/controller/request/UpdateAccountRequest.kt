package io.red.financesK.account.controller.request

import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class UpdateAccountRequest(
    @field:Size(max = 100, message = "Account name cannot exceed 100 characters")
    val accountName: String? = null,

    @field:Size(max = 255, message = "Account description cannot exceed 255 characters")
    val accountDescription: String? = null,

    val accountCurrentBalance: BigDecimal? = null,

    @field:Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    val accountCurrency: String? = null
)
