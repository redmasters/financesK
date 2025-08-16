package io.red.financesK.transaction.service.create

import io.red.financesK.account.service.search.SearchAccountService
import io.red.financesK.category.model.Category
import io.red.financesK.category.service.search.SearchCategoryService
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.global.utils.ConvertMoneyUtils
import io.red.financesK.transaction.controller.request.CreateTransactionRequest
import io.red.financesK.transaction.controller.response.CreateTransactionResponse
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.RecurrencePattern
import io.red.financesK.transaction.event.TransactionCreatedEvent
import io.red.financesK.transaction.model.InstallmentInfo
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.service.search.SearchUserService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Service
@Transactional
class CreateTransactionService(
    private val transactionRepository: TransactionRepository,
    private val searchCategoryService: SearchCategoryService,
    private val searchUserService: SearchUserService,
    private val searchAccountService: SearchAccountService,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(CreateTransactionService::class.java)

    fun execute(request: CreateTransactionRequest): List<CreateTransactionResponse> {
        log.info("m='execute', acao='criando transacao', request='$request'")

        val category = searchCategoryService.findCategoryById(request.categoryId)
        val user = searchUserService.findUserById(request.userId)

        if (request.recurrencePattern != null) {
            when (RecurrencePattern.fromString(request.recurrencePattern)) {
                RecurrencePattern.DAILY -> {
                    return recurringTransactions(request, category, user, RecurrencePattern.DAILY)
                }

                RecurrencePattern.WEEKLY -> {
                    return recurringTransactions(request, category, user, RecurrencePattern.WEEKLY)
                }

                RecurrencePattern.MONTHLY -> {
                    return recurringTransactions(request, category, user, RecurrencePattern.MONTHLY)
                }

                RecurrencePattern.YEARLY -> {
                    return recurringTransactions(request, category, user, RecurrencePattern.YEARLY)
                }

                else -> throw ValidationException("Invalid recurrence pattern")

            }
        } else {
            return singleTransaction(request, category, user)
        }
    }

    fun recurringTransactions(
        request: CreateTransactionRequest,
        category: Category,
        user: AppUser,
        recurrence: RecurrencePattern
    ): List<CreateTransactionResponse> {
        val account = searchAccountService.findAccountById(request.accountId)
        val amount = request.amount.toInt()
        val occurrences = request.totalInstallments ?: calculateOcurrences(request.dueDate, recurrence)

        val currentInstallment = request.currentInstallment ?: 1

        val installmentValue = if ((request.totalInstallments != null) && (occurrences > 0)) {
            amount.div(occurrences)
        } else {
            amount
        }

        val transactions = (currentInstallment..occurrences).map { occurrence ->
            val transactionDate = calculateTransactionDate(request.dueDate, recurrence, occurrence)
            val currentOccurrence = request.currentInstallment?.let {
                it + occurrence - currentInstallment
            } ?: (occurrence)

            val description = if (occurrences > 0 && request.totalInstallments?.let { it > 1 } == true) {
                "${request.description} - Parcela $currentOccurrence de $occurrences"
            } else {
                request.description
            }

            val status = if (request.status == PaymentStatus.PAID && occurrence > 1) {
                PaymentStatus.PENDING
            } else {
                request.status ?: PaymentStatus.PENDING
            }

            Transaction(
                description = description,
                amount = installmentValue,
                downPayment = request.downPayment?.let { ConvertMoneyUtils.convertToCents(it) },
                type = request.type,
                operationType = request.operationType,
                status = status,
                categoryId = category,
                dueDate = transactionDate,
                createdAt = Instant.now(),
                notes = request.notes,
                recurrencePattern = recurrence,
                installmentInfo = if ((occurrences > 1) && (request.totalInstallments != null)) {
                    InstallmentInfo(
                        totalInstallments = occurrences,
                        currentInstallment = currentOccurrence,
                        installmentValue = installmentValue
                    )
                } else null,
                userId = user,
                accountId = account
            )

        }

        val savedTransactions = transactionRepository.saveAll(transactions)
        log.info("m='recurringTransactions', acao='transacoes salvas', quantidade='${savedTransactions.size}', recorrencia='$recurrence'")

        return savedTransactions.map { transaction ->
            log.info("m='recurringTransactions', acao='publicando evento', transactionId='${transaction.id}'")
            publishEvent(transaction)
            CreateTransactionResponse(
                id = transaction.id ?: 0,
                "Transaction created successfully",
                transaction.createdAt ?: Instant.now()
            )
        }
    }


    private fun calculateTransactionDate(
        dueDate: LocalDate,
        recurrence: RecurrencePattern,
        occurrence: Int
    ): LocalDate {
        return when (recurrence) {
            RecurrencePattern.DAILY -> dueDate.plusDays(occurrence.toLong() - 1)
            RecurrencePattern.WEEKLY -> dueDate.plusWeeks(occurrence.toLong() - 1)
            RecurrencePattern.MONTHLY -> dueDate.plusMonths(occurrence.toLong() - 1)
            RecurrencePattern.YEARLY -> dueDate.plusYears(occurrence.toLong() - 1)
        }
    }

    fun singleTransaction(
        request: CreateTransactionRequest,
        category: Category,
        user: AppUser
    ): List<CreateTransactionResponse> {
        val account = searchAccountService.findAccountById(request.accountId)

        val transaction = Transaction(
            description = request.description,
            amount = request.amount.toInt(),
            downPayment = request.downPayment?.toInt(),
            type = request.type,
            operationType = request.operationType,
            status = request.status ?: PaymentStatus.PENDING,
            categoryId = category,
            dueDate = request.dueDate,
            createdAt = Instant.now(),
            notes = request.notes,
            recurrencePattern = null,
            installmentInfo = null,
            userId = user,
            accountId = account
        )

        val savedTransaction = transactionRepository.save(transaction)
        log.info("m='singleTransaction', acao='transacao salva', transactionId='${savedTransaction.id}'")

        // Publicar evento após salvar
        log.info("m='singleTransaction', acao='publicando evento', transactionId='${savedTransaction.id}'")
        publishEvent(savedTransaction)

        return listOf(
            CreateTransactionResponse(
                id = savedTransaction.id ?: 0,
                "Transaction created successfully",
                savedTransaction.createdAt ?: Instant.now()
            )
        )
    }

    private fun calculateOcurrences(
        dueDate: LocalDate,
        recurrence: RecurrencePattern
    ): Int {
        return when (recurrence) {
            RecurrencePattern.DAILY -> {
                val endOfMonth = dueDate.withDayOfMonth(dueDate.lengthOfMonth())
                ChronoUnit.DAYS.between(dueDate, endOfMonth).toInt() + 1
            }

            RecurrencePattern.WEEKLY -> {
                val endOfMonth = dueDate.withDayOfMonth(dueDate.lengthOfMonth())
                val totalDays = ChronoUnit.DAYS.between(dueDate, endOfMonth).toInt() + 1
                (totalDays / 7).coerceAtLeast(1)
            }

            RecurrencePattern.MONTHLY -> {
                val endOfYear = dueDate.withDayOfYear(dueDate.lengthOfYear())
                ChronoUnit.MONTHS.between(YearMonth.from(dueDate), YearMonth.from(endOfYear)).toInt() + 1
            }

            RecurrencePattern.YEARLY -> 1
        }
    }

    private fun publishEvent(transaction: Transaction) {
        applicationEventPublisher.publishEvent(
            TransactionCreatedEvent(
                transactionId = transaction.id ?: 0,
                accountId = transaction.accountId?.accountId ?: 0,
                amount = transaction.amount,
                type = transaction.type,
                status = transaction.status
            )
        )
    }

}
