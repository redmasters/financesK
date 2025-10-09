package io.red.financesK.transaction.service.search

import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.global.exception.NotFoundException
import io.red.financesK.global.utils.ConvertMoneyUtils
import io.red.financesK.global.utils.MoneyFormatterUtils
import io.red.financesK.transaction.controller.request.SearchTransactionFilter
import io.red.financesK.transaction.controller.response.AmountIncomeExpenseResponse
import io.red.financesK.transaction.controller.response.TransactionResponse
import io.red.financesK.transaction.controller.response.TransactionSearchResponse
import io.red.financesK.transaction.enums.SortDirection
import io.red.financesK.transaction.enums.TransactionSortField
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class SearchTransactionService(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) {
    private val log = LoggerFactory.getLogger(SearchTransactionService::class.java)

    fun getIncomeExpenseBalance(
        filter: SearchTransactionFilter
    ): AmountIncomeExpenseResponse {

        log.info(
            "m='getIncomeExpenseBalance', acao='iniciando busca de saldo', userId='${filter.userId}'," +
                    " type='${filter.type}', status='${filter.status}', startDate='${filter.startDate}'," +
                    " endDate='${filter.endDate}'"
        )

        // Buscar os totais das transações
        val transactionResult = transactionRepository.getIncomeExpenseBalance(
            userId = filter.userId,
            accountsId = filter.accountsId,
            status = filter.status,
            type = filter.type,
            categoryId = filter.categoryId,
            isRecurring = filter.isRecurring,
            hasInstallments = filter.hasInstallments,
            description = filter.description?.let { "%${it}%" }, // Adiciona wildcards para busca
            minAmount = filter.minAmount,
            maxAmount = filter.maxAmount,
            startDate = filter.startDate,
            endDate = filter.endDate
        )

        // Buscar o saldo total das contas
        val accountsBalance = filter.accountsId?.let { accountIds ->
            accountRepository.getTotalBalanceByUserIdAndAccountIds(filter.userId, accountIds)
        } ?: 0

        log.info("m='getIncomeExpenseBalance', acao='calculando saldo final', transactionBalance='${transactionResult.balance}', accountsBalance='$accountsBalance'")

        val finalBalance = ConvertMoneyUtils.convertToDecimal(accountsBalance)

        val result = AmountIncomeExpenseResponse(
            totalIncome = transactionResult.totalIncome,
            totalIncomeFormatted = transactionResult.totalIncomeFormatted,
            totalExpense = transactionResult.totalExpense,
            totalExpenseFormatted = transactionResult.totalExpenseFormatted,
            balance = finalBalance,
            balanceFormatted = MoneyFormatterUtils.formatToBrazilianCurrency(finalBalance)
        )

        log.info("m='getIncomeExpenseBalance', acao='balance calculado', totalIncome='${result.totalIncome}', totalExpense='${result.totalExpense}', balance='${result.balance}'")

        return result
    }

    fun searchTransactionsPaginated(
        filter: SearchTransactionFilter,
        page: Int,
        size: Int,
        sortField: TransactionSortField = TransactionSortField.DUE_DATE,
        sortDirection: SortDirection = SortDirection.DESC
    ): TransactionSearchResponse {
        log.info("m='searchTransactionsPaginated', acao='iniciando busca paginada', userId='${filter.userId}', page='$page', size='$size', sortField='$sortField', sortDirection='$sortDirection'")

        val pageable = when (sortField) {
            TransactionSortField.DUE_DATE_AND_STATUS -> {
                // Custom sorting: unpaid and due soon first, then paid transactions
                PageRequest.of(
                    page, size, Sort.by(
                        Sort.Order(Sort.Direction.ASC, "status"), // PENDING comes before PAID alphabetically
                        Sort.Order(Sort.Direction.ASC, "dueDate"),
                        Sort.Order(Sort.Direction.DESC, "paidAt") // For paid transactions, most recently paid first
                    )
                )
            }

            TransactionSortField.PAID_AT -> {
                PageRequest.of(
                    page, size, Sort.by(
                        Sort.Direction.valueOf(sortDirection.name), "paidAt"
                    )
                )
            }

            else -> {
                // Handle other sort fields as before
                PageRequest.of(
                    page, size, Sort.by(
                        Sort.Direction.valueOf(sortDirection.name), sortField.fieldName
                    )
                )
            }
        }

        // Preparar descrição para busca com wildcards (case-sensitive por enquanto)
        val searchDescription = filter.description?.let { "%${it}%" }

        val transactions = transactionRepository.findTransactionsByFilters(
            userId = filter.userId,
            accountsId = filter.accountsId,
            startDate = filter.startDate,
            endDate = filter.endDate,
            type = filter.type,
            status = filter.status,
            categoryId = filter.categoryId,
            isRecurring = filter.isRecurring,
            hasInstallments = filter.hasInstallments,
            description = searchDescription,
            minAmount = filter.minAmount,
            maxAmount = filter.maxAmount,
            pageable = pageable
        )

        log.info("m='searchTransactionsPaginated', acao='busca concluida', totalElements='${transactions.totalElements}', totalPages='${transactions.totalPages}'")

        val finacialSummary = getIncomeExpenseBalance(filter)

        return TransactionSearchResponse(
            transactions = transactions.map { convertToTransactionResponse(it) },
            totalIncome = finacialSummary.totalIncome,
            totalIncomeFormatted = finacialSummary.totalIncomeFormatted,
            totalExpense = finacialSummary.totalExpense,
            totalExpenseFormatted = finacialSummary.totalExpenseFormatted,
            balance = finacialSummary.balance,
            balanceFormatted = finacialSummary.balanceFormatted
        )
    }

    private fun convertToTransactionResponse(transaction: Transaction): TransactionResponse {
        return TransactionResponse(
            id = transaction.id!!,
            description = transaction.description,
            amount = ConvertMoneyUtils.convertToDecimal(transaction.amount),
            downPayment = transaction.downPayment?.let { ConvertMoneyUtils.convertToDecimal(it) },
            type = transaction.type!!,
            operationType = transaction.operationType!!,
            status = transaction.status!!,
            categoryId = transaction.categoryId.id ?: 0,
            categoryName = transaction.categoryId.name,
            dueDate = transaction.dueDate,
            createdAt = transaction.createdAt,
            updatedAt = transaction.updatedAt,
            notes = transaction.notes,
            recurrencePattern = transaction.recurrencePattern,
            installmentInfo = transaction.installmentInfo,
            userId = transaction.userId.id ?: 0,
            accountId = transaction.accountId?.accountId ?: 0,
            accountName = transaction.accountId?.accountName
        )
    }

    fun getTransactionById(transactionId: Int?): TransactionResponse {
        log.info("m='findTransactionById', acao='buscando transação por ID', transactionId='$transactionId'")

        val transaction = transactionRepository.findById(transactionId!!)
            .orElseThrow { NotFoundException("Transaction with id $transactionId not found") }
        log.info("m='findTransactionById', acao='transação encontrada', transaction='$transaction'")
        return convertToTransactionResponse(transaction)
    }
}
