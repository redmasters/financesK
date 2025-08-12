package io.red.financesK.transaction.service.update

import io.red.financesK.global.exception.NotFoundException
import io.red.financesK.transaction.controller.request.UpdateTransactionRequest
import io.red.financesK.transaction.controller.response.UpdateTransactionResponse
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.transaction.service.search.SearchCategoryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UpdateTransactionService(
    private val transactionRepository: TransactionRepository,
    private val searchCategoryService: SearchCategoryService
) {
    private val log = LoggerFactory.getLogger(UpdateTransactionService::class.java)

    fun updateTransaction(
        transactionId: Int,
        updateRequest: UpdateTransactionRequest
    ): UpdateTransactionResponse {
        log.info("m='updateTransaction', transactionId='$transactionId', updateRequest='$updateRequest'")
        val transaction = transactionRepository.findById(transactionId)
            .orElseThrow { NotFoundException("Transaction with id $transactionId not found") }
        val category = searchCategoryService.findCategoryById(updateRequest.categoryId)

        log.info("m='updateTransaction', transactionFound='$transaction'")
        transaction.apply {
            updateRequest.description?.let { description = it }
            updateRequest.amount?.let { amount = it.toInt() }
            updateRequest.downPayment?.let { downPayment = it.toInt() }
            updateRequest.type?.let { type = it }
            updateRequest.operationType?.let { operationType = it }
            updateRequest.status?.let { status = it }
            updateRequest.categoryId?.let { categoryId = category }
            updateRequest.dueDate?.let { dueDate = it }
            updateRequest.notes?.let { notes = it }
            // Handle recurrencePattern and installmentInfo if needed
        }
        transactionRepository.save(transaction)
        log.info("m='updateTransaction', transactionUpdated='$transaction'")
        return UpdateTransactionResponse(
            id = transaction.id ?: 0,
            updatedAt = transaction.updatedAt ?: Instant.now()

        )
    }
}

