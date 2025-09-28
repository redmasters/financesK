package io.red.financesK.transaction.controller.response

import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.global.utils.MoneyFormatterUtils
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.RecurrencePattern
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.model.InstallmentInfo
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class TransactionResponse(
    val id: Int,
    val description: String,
    val amount: BigDecimal,
    val amountFormatted: String,
    val downPayment: BigDecimal? = null,
    val downPaymentFormatted: String? = null,
    val type: TransactionType,
    val operationType: AccountOperationType,
    val status: PaymentStatus,
    val categoryId: Int,
    val categoryName: String? = null,
    val dueDate: LocalDate,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val notes: String? = null,
    val recurrencePattern: RecurrencePattern? = null,
    val installmentInfo: InstallmentInfo? = null,
    val userId: Int,
    val accountId: Int,
    val accountName: String? = null
) {
    constructor(
        id: Int,
        description: String,
        amount: BigDecimal,
        downPayment: BigDecimal? = null,
        type: TransactionType,
        operationType: AccountOperationType,
        status: PaymentStatus,
        categoryId: Int,
        categoryName: String? = null,
        dueDate: LocalDate,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        notes: String? = null,
        recurrencePattern: RecurrencePattern? = null,
        installmentInfo: InstallmentInfo? = null,
        userId: Int,
        accountId: Int,
        accountName: String? = null
    ) : this(
        id = id,
        description = description,
        amount = amount,
        amountFormatted = MoneyFormatterUtils.formatToBrazilianCurrency(amount),
        downPayment = downPayment,
        downPaymentFormatted = MoneyFormatterUtils.formatToBrazilianCurrency(downPayment),
        type = type,
        operationType = operationType,
        status = status,
        categoryId = categoryId,
        categoryName = categoryName,
        dueDate = dueDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        notes = notes,
        recurrencePattern = recurrencePattern,
        installmentInfo = installmentInfo,
        userId = userId,
        accountId = accountId,
        accountName = accountName
    )
}
