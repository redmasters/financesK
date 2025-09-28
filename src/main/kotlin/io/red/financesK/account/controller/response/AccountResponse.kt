package io.red.financesK.account.controller.response

import io.red.financesK.global.utils.DateTimeUtils
import io.red.financesK.global.utils.MoneyFormatterUtils
import java.math.BigDecimal
import java.time.Instant

data class AccountResponse(
    val accountId: Int,
    val accountName: String,
    val accountDescription: String? = null,
    val accountType: String? = null,
    val bankInstitutionName: String? = null,
    val accountCreditLimit: BigDecimal? = null,
    val accountCreditLimitFormatted: String? = null,
    val accountStatementClosingDate: Int? = null,
    val accountPaymentDueDate: Int? = null,
    val accountStatementClosingDateFormatted: String? = null,
    val accountPaymentDueDateFormatted: String? = null,
    val accountCurrentBalance: BigDecimal,
    val accountCurrentBalanceFormatted: String,
    val accountCurrency: String,
    val userId: Int,
    val userName: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
) {
    constructor(
        accountId: Int,
        accountName: String,
        accountDescription: String? = null,
        accountType: String? = null,
        bankInstitutionName: String? = null,
        accountCreditLimit: BigDecimal? = null,
        accountStatementClosingDate: Int? = null,
        accountPaymentDueDate: Int? = null,
        accountCurrentBalance: BigDecimal,
        accountCurrency: String,
        userId: Int,
        userName: String? = null,
        createdAt: Instant? = null,
        updatedAt: Instant? = null
    ) : this(
        accountId = accountId,
        accountName = accountName,
        accountDescription = accountDescription,
        accountType = accountType,
        bankInstitutionName = bankInstitutionName,
        accountCreditLimit = accountCreditLimit,
        accountCreditLimitFormatted = MoneyFormatterUtils.formatToBrazilianCurrency(
            accountCreditLimit ?: BigDecimal.ZERO
        ),
        accountStatementClosingDate = accountStatementClosingDate,
        accountPaymentDueDate = accountPaymentDueDate,
        accountStatementClosingDateFormatted = accountStatementClosingDate?.let {
            DateTimeUtils.formatToBrazilianDateByDayInput(
                it
            )
        },
        accountPaymentDueDateFormatted = accountPaymentDueDate?.let {
            DateTimeUtils.formatToBrazilianDateByDayInput(
                it
            )
        },
        accountCurrentBalance = accountCurrentBalance,
        accountCurrentBalanceFormatted = MoneyFormatterUtils.formatToBrazilianCurrency(accountCurrentBalance),
        accountCurrency = accountCurrency,
        userId = userId,
        userName = userName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
