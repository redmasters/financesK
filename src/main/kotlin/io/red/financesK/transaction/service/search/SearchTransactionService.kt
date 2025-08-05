package io.red.financesK.transaction.service.search

import io.red.financesK.transaction.controller.request.SearchTransactionFilter
import io.red.financesK.transaction.controller.response.TransactionResponse
import io.red.financesK.transaction.repository.custom.TransactionCustomRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class SearchTransactionService(
    private val transactionCustomRepository: TransactionCustomRepository
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
        return transactions.map { entity ->
            TransactionResponse(
                id = entity.id!!,
                description = entity.description,
                amount = entity.amount,
                type = entity.type?.name ?: "UNKNOWN",
                categoryId = entity.categoryId?.id ?: 0,
                transactionDate = entity.transactionDate,
                createdAt = entity.createdAt,
                notes = entity.notes,
                recurrencePattern = entity.recurrencePattern?.name,
                installmentInfo = entity.installmentInfo,
                userId = entity.userId?.id ?: 0
            )
        }
    }
}
