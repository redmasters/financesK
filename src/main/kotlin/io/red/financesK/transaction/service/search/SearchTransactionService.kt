package io.red.financesK.transaction.service.search

import io.red.financesK.transaction.controller.response.AmountIncomeExpenseResponse
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SearchTransactionService(
    private val transactionRepository: TransactionRepository
) {
    private val log = LoggerFactory.getLogger(SearchTransactionService::class.java)

    fun getIncomeExpenseBalance(
        userId: Int,
        status: PaymentStatus?,
        startDate: LocalDate,
        endDate: LocalDate
    ): AmountIncomeExpenseResponse {
        log.info("m='getIncomeExpenseBalance', acao='calculando balance receitas/despesas', userId='$userId', startDate='$startDate', endDate='$endDate'")

        val result = transactionRepository.getIncomeExpenseBalance(userId, status, startDate, endDate)

        log.info("m='getIncomeExpenseBalance', acao='balance calculado', totalIncome='${result.totalIncome}', totalExpense='${result.totalExpense}', balance='${result.balance}'")

        return result
    }

    fun sumAmountByUserIdAndTypeAndDateRange(
        userId: Int,
        type: String,
        status: String,
        startDate: String,
        endDate: String
    ): AmountIncomeExpenseResponse {
        val startDateLocal = LocalDate.parse(startDate)
        val endDateLocal = LocalDate.parse(endDate)
        val transactionType = TransactionType.fromString(type)
        val paymentStatus = PaymentStatus.fromString(status)

        // Para manter compatibilidade, vamos buscar ambos os tipos e calcular
        val incomeAmount = if (transactionType == TransactionType.INCOME) {
            transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.INCOME, paymentStatus, startDateLocal, endDateLocal
            ) ?: 0
        } else 0

        val expenseAmount = if (transactionType == TransactionType.EXPENSE) {
            transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.EXPENSE, paymentStatus, startDateLocal, endDateLocal
            ) ?: 0
        } else 0

        val totalIncome = incomeAmount
        val totalExpense = expenseAmount
        val balance = totalIncome - totalExpense

        return AmountIncomeExpenseResponse(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance
        )
    }
}
