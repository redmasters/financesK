package io.red.financesK.account.controller.response

import java.time.Instant

data class CreateAccountResponse(
    val accountId: Int?,
    val message: String = "Account created successfully",
    val createdAt: Instant?
)
