package io.red.financesK.transaction.service.create

import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.account.balance.service.update.UpdateBalanceService
import io.red.financesK.account.service.search.SearchAccountService
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.transaction.controller.request.CreateTransactionRequest
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.RecurrencePattern
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.model.Category
import io.red.financesK.transaction.model.InstallmentInfo
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.repository.CategoryRepository
import io.red.financesK.transaction.repository.TransactionRepository
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Service
class CreateTransactionService(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val appUserRepository: AppUserRepository,
    private val searchAccountService: SearchAccountService,
    private val updateBalanceService: UpdateBalanceService
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


        if (request.recurrencePattern != null) {
            when (RecurrencePattern.fromString(request.recurrencePattern)) {
                RecurrencePattern.DAILY -> createRecurringTransactions(request, category, user, RecurrencePattern.DAILY)
                RecurrencePattern.WEEKLY -> createRecurringTransactions(
                    request,
                    category,
                    user,
                    RecurrencePattern.WEEKLY
                )

                RecurrencePattern.MONTHLY -> createRecurringTransactions(
                    request,
                    category,
                    user,
                    RecurrencePattern.MONTHLY
                )

                RecurrencePattern.YEARLY -> createRecurringTransactions(
                    request,
                    category,
                    user,
                    RecurrencePattern.YEARLY
                )

                else -> throw ValidationException("Invalid recurrence pattern")
            }
        } else {
            createSingleTransaction(request, category, user)
        }
    }

    private fun createRecurringTransactions(
        request: CreateTransactionRequest,
        category: Category,
        user: AppUser,
        recurrencePattern: RecurrencePattern
    ) {
        log.info(
            "m='createRecurringTransactions', acao='criando transações recorrentes', pattern='{}'",
            recurrencePattern.name
        )

        val account = searchAccountService.searchAccountById(request.accountId)

        val occurrences = if (request.totalInstallments > 0) {
            request.totalInstallments
        } else {
            calculateOccurrences(request.dueDate, recurrencePattern)
        }
        val installmentValue = if (recurrencePattern == RecurrencePattern.MONTHLY && request.totalInstallments > 1) {
            request.amount.divide(BigDecimal(request.totalInstallments), 2, RoundingMode.HALF_DOWN)
        } else {
            request.amount
        }

        val transactions = (1..occurrences).map { occurrence ->
            val transactionDate = calculateTransactionDate(request.dueDate, occurrence - 1, recurrencePattern)

            val currentOccurrence =
                if (request.currentInstallment != null && recurrencePattern == RecurrencePattern.MONTHLY) {
                    request.currentInstallment
                } else {
                    occurrence
                }

            val description = if (occurrences > 1) {
                "${request.description} ($currentOccurrence/$occurrences)"
            } else {
                request.description
            }

            Transaction(
                description = description,
                amount = installmentValue,
                downPayment = request.downPayment,
                type = TransactionType.fromString(request.type),
                operationType = request.operationType?.let { AccountOperationType.fromString(it) },
                status = request.status?.let { PaymentStatus.fromString(it) } ?: PaymentStatus.PENDING,
                categoryId = category,
                dueDate = transactionDate,
                createdAt = Instant.now(),
                notes = request.notes,
                recurrencePattern = recurrencePattern,
                installmentInfo = if (occurrences > 1) {
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

        transactionRepository.saveAll(transactions)
        log.info("m='createRecurringTransactions', acao='transações criadas', total='{}'", transactions.size)
    }

    private fun calculateOccurrences(startDate: LocalDate, recurrencePattern: RecurrencePattern): Int {
        return when (recurrencePattern) {
            RecurrencePattern.DAILY -> {
                val endOfMonth = startDate.withDayOfMonth(startDate.lengthOfMonth())
                ChronoUnit.DAYS.between(startDate, endOfMonth).toInt() + 1
            }

            RecurrencePattern.WEEKLY -> {
                val endOfMonth = startDate.withDayOfMonth(startDate.lengthOfMonth())
                val totalDays = ChronoUnit.DAYS.between(startDate, endOfMonth).toInt() + 1
                (totalDays / 7).coerceAtLeast(1)
            }

            RecurrencePattern.MONTHLY -> {
                val endOfYear = startDate.withDayOfYear(startDate.lengthOfYear())
                ChronoUnit.MONTHS.between(YearMonth.from(startDate), YearMonth.from(endOfYear)).toInt() + 1
            }

            RecurrencePattern.YEARLY -> 1
        }
    }

    private fun calculateTransactionDate(
        startDate: LocalDate,
        occurrence: Int,
        recurrencePattern: RecurrencePattern
    ): LocalDate {
        return when (recurrencePattern) {
            RecurrencePattern.DAILY -> startDate.plusDays(occurrence.toLong())
            RecurrencePattern.WEEKLY -> startDate.plusWeeks(occurrence.toLong())
            RecurrencePattern.MONTHLY -> startDate.plusMonths(occurrence.toLong())
            RecurrencePattern.YEARLY -> startDate.plusYears(occurrence.toLong())
        }
    }

    private fun createSingleTransaction(
        request: CreateTransactionRequest,
        category: Category,
        user: AppUser
    ) {
        log.info("m='createSingleTransaction', acao='criando transação única'")

        val account = request.accountId?.let { searchAccountService.searchAccountById(it) }
            ?: throw ValidationException("Account ID must be provided for single transactions")

        val transaction = Transaction(
            description = request.description,
            amount = request.amount,
            type = TransactionType.fromString(request.type),
            operationType = request.operationType?.let { AccountOperationType.fromString(it) },
            status = request.status?.let { PaymentStatus.fromString(it) } ?: PaymentStatus.PENDING,
            categoryId = category,
            dueDate = request.dueDate,
            createdAt = Instant.now(),
            notes = request.notes,
            recurrencePattern = null,
            installmentInfo = null,
            userId = user,
            accountId = account
        )

        transactionRepository.save(transaction)
        updateBalanceService.executeOperation(transaction)
        log.info("m='createSingleTransaction',acao='transação única criada'")
    }



}
