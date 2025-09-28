package io.red.financesK.transaction.event

import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType

data class TransactionCreatedEvent(
    val transactionId: Int,
    val accountId: Int,
    val amount: Int,
    val type: TransactionType?,
    val status: PaymentStatus?
)
