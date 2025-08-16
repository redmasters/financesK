package io.red.financesK.transaction.service.search

import io.red.financesK.account.service.search.SearchAccountService
import io.red.financesK.global.exception.NotFoundException
import io.red.financesK.global.utils.ConvertMoneyUtils
import io.red.financesK.transaction.controller.request.SearchTransactionFilter
import io.red.financesK.transaction.controller.response.AmountIncomeExpenseResponse
import io.red.financesK.transaction.controller.response.TransactionResponse
import io.red.financesK.transaction.enums.SortDirection
import io.red.financesK.transaction.enums.TransactionSortField
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class SearchTransactionService(
    private val transactionRepository: TransactionRepository,
    private val searchAccountService: SearchAccountService
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

        val result = transactionRepository.getIncomeExpenseBalance(
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

        log.info("m='getIncomeExpenseBalance', acao='balance calculado', totalIncome='${result.totalIncome}', totalExpense='${result.totalExpense}', balance='${result.balance}'")

        return result
    }

    fun searchTransactionsPaginated(
        filter: SearchTransactionFilter,
        page: Int,
        size: Int,
        sortField: TransactionSortField = TransactionSortField.DUE_DATE,
        sortDirection: SortDirection = SortDirection.DESC
    ): Page<TransactionResponse> {
        log.info("m='searchTransactionsPaginated', acao='iniciando busca paginada', userId='${filter.userId}', page='$page', size='$size', sortField='$sortField', sortDirection='$sortDirection'")

        val sort = when (sortDirection) {
            SortDirection.ASC -> Sort.by(Sort.Direction.ASC, sortField.fieldName)
            SortDirection.DESC -> Sort.by(Sort.Direction.DESC, sortField.fieldName)
        }

        val pageable = PageRequest.of(page, size, sort)

        // Preparar descrição para busca com wildcards (case-sensitive por enquanto)
        val searchDescription = filter.description?.let { "%${it}%" }

        val transactions = transactionRepository.findTransactionsByFilters(
            userId = filter.userId,
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

        return transactions.map { transaction ->
            convertToTransactionResponse(transaction)
        }
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
