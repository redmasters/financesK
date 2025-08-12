package io.red.financesK.transaction.service.delete

import io.red.financesK.transaction.controller.response.DeleteTransactionResponse
import io.red.financesK.transaction.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DeleteTransactionService(
    private val transactionRepository: TransactionRepository,
) {
    private val log = LoggerFactory.getLogger(DeleteTransactionService::class.java)

    fun deleteTransaction(transactionId: Int): DeleteTransactionResponse {
        log.info("m='deleteTransaction', transactionId='$transactionId'")

        val transaction = transactionRepository.findById(transactionId)
            .orElseThrow { IllegalArgumentException("Transaction with id $transactionId not found") }
        transactionRepository.delete(transaction)
        log.info("m='deleteTransaction', transactionDeleted='$transactionId'")

        return DeleteTransactionResponse(
            id = transactionId
        )
    }

}
