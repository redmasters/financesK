package io.red.financesK.dashboard.controller.response

import java.math.BigDecimal

data class MonthlyFinancialOverviewResponse(
    val monthlyBalance: MonthlyBalance,
    val salary: Salary,
    val totalExpenses: ExpenseInfo,
    val recurringExpenses: ExpenseInfo,
    val installmentExpenses: ExpenseInfo
)

data class MonthlyBalance(
    val value: BigDecimal,
    val currency: String,
    val updatedAt: String,
    val progress: BalanceProgress
)

data class BalanceProgress(
    val monthElapsed: String,
    val remainingBalance: String
)

data class Salary(
    val value: BigDecimal,
    val currency: String,
    val receivedAt: String
)

data class ExpenseInfo(
    val value: BigDecimal,
    val currency: String,
    val percentageOfSalary: String
)
