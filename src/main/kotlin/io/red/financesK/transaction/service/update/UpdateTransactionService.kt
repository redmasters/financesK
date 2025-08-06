package io.red.financesK.transaction.service.update

import io.red.financesK.global.exception.ValidationException
import io.red.financesK.transaction.controller.request.UpdateTransactionRequest
import io.red.financesK.transaction.controller.response.TransactionResponse
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.RecurrencePattern
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.model.InstallmentInfo
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.repository.CategoryRepository
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.user.repository.AppUserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class UpdateTransactionService(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val appUserRepository: AppUserRepository
) {
    private val log = LoggerFactory.getLogger(UpdateTransactionService::class.java)

    fun execute(id: Int, request: UpdateTransactionRequest): TransactionResponse {
        log.info("m='execute', acao='atualizando transação', id='{}', request='{}'", id, request)

        val existingTransaction = transactionRepository.findById(id).orElseThrow {
            ValidationException("Transaction with id $id not found")
        }

        // Validações
        request.amount?.let { amount ->
            if (amount <= BigDecimal.ZERO) {
                throw ValidationException("Transaction amount must be greater than zero")
            }
        }

        // Buscar categoria se foi fornecida
        val category = request.categoryId?.let { categoryId ->
            categoryRepository.findById(categoryId).orElseThrow {
                ValidationException("Category with id $categoryId not found")
            }
        } ?: existingTransaction.categoryId

        // Buscar usuário se foi fornecido (opcional - geralmente não seria alterado)
        val user = existingTransaction.userId

        // Validar e converter tipo de transação
        val transactionType = request.type?.let { typeStr ->
            try {
                TransactionType.valueOf(typeStr.uppercase())
            } catch (e: IllegalArgumentException) {
                throw ValidationException("Invalid transaction type: $typeStr")
            }
        } ?: existingTransaction.type

        // Validar e converter status de pagamento
        val paymentStatus = request.status?.let { statusStr ->
            try {
                PaymentStatus.valueOf(statusStr.uppercase())
            } catch (e: IllegalArgumentException) {
                throw ValidationException("Invalid payment status: $statusStr")
            }
        } ?: existingTransaction.status

        // Validar e converter padrão de recorrência
        val recurrencePattern = request.recurrencePattern?.let { patternStr ->
            RecurrencePattern.fromString(patternStr) ?: throw ValidationException("Invalid recurrence pattern: $patternStr")
        } ?: existingTransaction.recurrencePattern

        // Criar nova informação de parcelas se necessário
        val installmentInfo = if (request.totalInstallments != null || request.currentInstallment != null) {
            val totalInstallments = request.totalInstallments ?: existingTransaction.installmentInfo?.totalInstallments ?: 0
            val currentInstallment = request.currentInstallment ?: existingTransaction.installmentInfo?.currentInstallment ?: 1
            val installmentValue = request.amount ?: existingTransaction.amount

            if (totalInstallments > 1 && currentInstallment > totalInstallments) {
                throw ValidationException("Current installment cannot be greater than total installments")
            }

            InstallmentInfo(
                totalInstallments = totalInstallments,
                currentInstallment = currentInstallment,
                installmentValue = installmentValue
            )
        } else {
            existingTransaction.installmentInfo
        }

        // Criar transação atualizada
        val updatedTransaction = Transaction(
            id = existingTransaction.id,
            description = request.description ?: existingTransaction.description,
            amount = request.amount ?: existingTransaction.amount,
            downPayment = request.downPayment ?: existingTransaction.downPayment,
            type = transactionType,
            status = paymentStatus,
            categoryId = category,
            dueDate = request.dueDate ?: existingTransaction.dueDate,
            updatedAt = Instant.now(),
            notes = request.notes ?: existingTransaction.notes,
            recurrencePattern = recurrencePattern,
            installmentInfo = installmentInfo,
            userId = user
        )

        val savedTransaction = transactionRepository.save(updatedTransaction)
        log.info("m='execute', acao='transação atualizada com sucesso', id='{}'", id)

        return TransactionResponse(
            id = savedTransaction.id!!,
            description = savedTransaction.description,
            amount = savedTransaction.amount,
            downPayment = savedTransaction.downPayment,
            type = savedTransaction.type?.name ?: "UNKNOWN",
            status = PaymentStatus.toString(savedTransaction.status),
            categoryId = savedTransaction.categoryId.id!!,
            dueDate = savedTransaction.dueDate,
            createdAt = savedTransaction.createdAt,
            updatedAt = savedTransaction.updatedAt,
            notes = savedTransaction.notes,
            recurrencePattern = RecurrencePattern.toString(savedTransaction.recurrencePattern),
            installmentInfo = InstallmentInfo(
                totalInstallments = savedTransaction.installmentInfo?.totalInstallments,
                currentInstallment = savedTransaction.installmentInfo?.currentInstallment,
                installmentValue = savedTransaction.installmentInfo?.installmentValue
            ),
            userId = savedTransaction.userId.id!!
        )
    }
}
