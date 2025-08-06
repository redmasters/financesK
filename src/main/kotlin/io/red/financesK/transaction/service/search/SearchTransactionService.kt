package io.red.financesK.transaction.service.search

import io.red.financesK.transaction.controller.request.SearchTransactionFilter
import io.red.financesK.transaction.controller.response.TransactionResponse
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.transaction.repository.custom.TransactionCustomRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class SearchTransactionService(
    private val transactionCustomRepository: TransactionCustomRepository,
    private val transactionRepository: TransactionRepository
) {
    private val log = LoggerFactory.getLogger(SearchTransactionService::class.java)

    fun execute(
        filter: SearchTransactionFilter,
        pageable: Pageable
    ): Page<TransactionResponse> {
        log.info(
            "m='execute', acao='buscando transações', filtro='{}', page={}, size={}",
            filter, pageable.pageNumber, pageable.pageSize
        )
        val transactions = transactionCustomRepository.findByDynamicFilter(filter, pageable)
        return transactions.map { entity -> toTransactionResponse(entity) }
    }


    fun searchById(id: Int): TransactionResponse? {
        log.info("m='searchById', acao='buscando transação por id', id='{}'", id)
        val transaction = transactionRepository.findById(id).orElse(null) ?: run {
            log.info("m='searchById', acao='transação não encontrada', id='{}'", id)
            return null
        }
        log.info("m='searchById', acao='transação encontrada', id='{}'", id)
        return toTransactionResponse(transaction)
    }


    private fun toTransactionResponse(entity: Transaction): TransactionResponse {
        return TransactionResponse(
            id = entity.id!!,
            description = entity.description,
            amount = entity.amount,
            downPayment = entity.downPayment,
            type = entity.type?.name ?: "UNKNOWN",
            status = entity.status.toString(),
            categoryId = entity.categoryId.id!!,
            dueDate = entity.dueDate,
            createdAt = entity.createdAt,
            notes = entity.notes,
            recurrencePattern = entity.recurrencePattern?.name,
            installmentInfo = entity.installmentInfo,
            userId = entity.userId.id!!
        )
    }
}
