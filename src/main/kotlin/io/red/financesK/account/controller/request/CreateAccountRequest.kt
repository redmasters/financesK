package io.red.financesK.account.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class CreateAccountRequest(
    @field:NotBlank(message = "Account name is required")
    @field:Size(max = 100, message = "Account name cannot exceed 100 characters")
    val accountName: String,

    @field:Size(max = 255, message = "Account description cannot exceed 255 characters")
    val accountDescription: String? = null,

    val accountCurrentBalance: BigDecimal? = BigDecimal.ZERO,

    @field:Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    val accountCurrency: String = "BRL",

    @field:NotNull(message = "User ID is required")
    val userId: Int
)
