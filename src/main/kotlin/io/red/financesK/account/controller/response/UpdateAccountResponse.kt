package io.red.financesK.account.controller.response

import java.time.Instant

data class UpdateAccountResponse(
    val accountId: Int,
    val message: String = "Account updated successfully",
    val updatedAt: Instant
)
