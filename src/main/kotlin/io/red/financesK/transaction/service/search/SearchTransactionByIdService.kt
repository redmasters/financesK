package io.red.financesK.transaction.service.search

import io.red.financesK.transaction.controller.response.TransactionResponse
import io.red.financesK.transaction.model.InstallmentInfo
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SearchTransactionByIdService(
    private val transactionRepository: TransactionRepository
) {
    private val log = LoggerFactory.getLogger(SearchTransactionByIdService::class.java)

    fun execute(id: Int): TransactionResponse? {
        log.info("m='execute', acao='buscando transação por id', id='{}'", id)
        val transaction = transactionRepository.findById(id).orElse(null) ?: run {
            log.info("m='execute', acao='transação não encontrada', id='{}'", id)
            return null
        }
        log.info("m='execute', acao='transação encontrada', id='{}'", id)
        return TransactionResponse(
            id = transaction.id!!,
            description = transaction.description,
            amount = transaction.amount,
            type = transaction.type?.name ?: "UNKNOWN",
            categoryId = transaction.categoryId.id!!,
            transactionDate = transaction.transactionDate,
            createdAt = transaction.createdAt,
            notes = transaction.notes,
            recurrencePattern = Transaction.RecurrencePattern.toString(transaction.recurrencePattern),
            installmentInfo = InstallmentInfo(
                totalInstallments = transaction.installmentInfo?.totalInstallments,
                currentInstallment = transaction.installmentInfo?.currentInstallment,
                installmentValue = transaction.installmentInfo?.installmentValue
            ),
            userId = transaction.userId.id!!
        )
    }
}
