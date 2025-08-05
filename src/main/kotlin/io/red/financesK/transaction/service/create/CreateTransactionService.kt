package io.red.financesK.transaction.service.create

import io.red.financesK.global.exception.ValidationException
import io.red.financesK.transaction.controller.request.CreateTransactionRequest
import io.red.financesK.transaction.model.InstallmentInfo
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.repository.CategoryRepository
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.user.repository.AppUserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

@Service
class CreateTransactionService(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val appUserRepository: AppUserRepository
) {
    private val log = LoggerFactory.getLogger(CreateTransactionService::class.java)

    fun execute(request: CreateTransactionRequest) {
        log.info("m='execute', acao='criando transação', request='{}'", request)

        if (request.amount <= BigDecimal.ZERO) {
            throw ValidationException("Transaction amount must be greater than zero")
        }

        val category = categoryRepository.findById(request.categoryId).orElseThrow {
            IllegalArgumentException("Category with id ${request.categoryId} not found")
        }

        val user = appUserRepository.findById(request.userId).orElseThrow {
            IllegalArgumentException("User with id ${request.userId} not found")
        }
        val installmentValue = request.amount.divide(
            BigDecimal(request.totalInstallments), 2,
            RoundingMode.HALF_UP
        );

        (1..request.totalInstallments).map { installmentNumber ->
            val installmentDate = request.startDate.plusMonths((installmentNumber - 1).toLong())

            Transaction(
                description = "${request.description} ($installmentNumber/${request.totalInstallments})",
                amount = installmentValue,
                type = Transaction.TransactionType.fromString(request.type),
                categoryId = category,
                transactionDate = installmentDate,
                createdAt = Instant.now(),
                notes = request.notes,
                recurrencePattern = Transaction.RecurrencePattern.fromString(request.recurrencePattern),
                installmentInfo = InstallmentInfo(
                    totalInstallments = request.totalInstallments,
                    currentInstallment = installmentNumber,
                    installmentValue = installmentValue
                ),
                userId = user

            )


        }.also {
            transactionRepository.saveAll(it)
        }

    }
}
