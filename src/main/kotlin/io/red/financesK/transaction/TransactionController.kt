package io.red.financesK.transaction

import io.red.financesK.transaction.controller.request.CreateTransactionRequest
import io.red.financesK.transaction.controller.request.SearchTransactionFilter
import io.red.financesK.transaction.controller.response.AmountIncomeExpenseResponse
import io.red.financesK.transaction.controller.response.CreateTransactionResponse
import io.red.financesK.transaction.controller.response.TransactionResponse
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.SortDirection
import io.red.financesK.transaction.enums.TransactionSortField
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.service.create.CreateTransactionService
import io.red.financesK.transaction.service.search.SearchTransactionService
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = ["*"])
class TransactionController(
    private val createTransactionService: CreateTransactionService,
    private val searchTransactionService: SearchTransactionService
) {
    @PostMapping
    fun createTransaction(
        @RequestBody request: CreateTransactionRequest
    ): ResponseEntity<List<CreateTransactionResponse>> {
        val transaction: List<CreateTransactionResponse> =
            createTransactionService.execute(request)
        return ResponseEntity.status(201).body(
            transaction.map {
                CreateTransactionResponse(
                    id = it.id,
                    message = "Transaction created successfully",
                    createdAt = it.createdAt
                )
            })
    }

    @GetMapping("/sumAmount")
    fun getSumAmountByUserIdAndTypeAndDateRange(
        @RequestParam userId: Int,
        @RequestParam type: String,
        @RequestParam status: String,
        @RequestParam startDate: String,
        @RequestParam endDate: String
    ): ResponseEntity<AmountIncomeExpenseResponse> {
        val sumAmount = searchTransactionService.sumAmountByUserIdAndTypeAndDateRange(
            userId, type, status, startDate, endDate
        )
        return ResponseEntity.ok(sumAmount)
    }

    @GetMapping("/stats/income-expense-balance")
    fun getIncomeExpenseBalance(
        @RequestParam userId: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam startDate: String,
        @RequestParam endDate: String
    ): ResponseEntity<AmountIncomeExpenseResponse> {
        val paymentStatus = status?.let { PaymentStatus.valueOf(it.uppercase()) }
        val balance = searchTransactionService.getIncomeExpenseBalance(
            userId = userId,
            status = paymentStatus,
            startDate = LocalDate.parse(startDate),
            endDate = LocalDate.parse(endDate)
        )
        return ResponseEntity.ok(balance)
    }

    @GetMapping("/search")
    fun searchTransactionsPaginated(
        @RequestParam userId: Int,
        @RequestParam startDate: String,
        @RequestParam endDate: String,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) categoryId: Int?,
        @RequestParam(required = false) isRecurring: Boolean?,
        @RequestParam(required = false) hasInstallments: Boolean?,
        @RequestParam(required = false) description: String?,
        @RequestParam(required = false) minAmount: Int?,
        @RequestParam(required = false) maxAmount: Int?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "DUE_DATE") sortField: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<Page<TransactionResponse>> {

        val filter = SearchTransactionFilter(
            userId = userId,
            startDate = LocalDate.parse(startDate),
            endDate = LocalDate.parse(endDate),
            type = type?.let { TransactionType.valueOf(it.uppercase()) },
            status = status?.let { PaymentStatus.valueOf(it.uppercase()) },
            categoryId = categoryId,
            isRecurring = isRecurring,
            hasInstallments = hasInstallments,
            description = description,
            minAmount = minAmount,
            maxAmount = maxAmount
        )

        val sortFieldEnum = TransactionSortField.valueOf(sortField.uppercase())
        val sortDirectionEnum = SortDirection.valueOf(sortDirection.uppercase())

        val result = searchTransactionService.searchTransactionsPaginated(
            filter = filter,
            page = page,
            size = size,
            sortField = sortFieldEnum,
            sortDirection = sortDirectionEnum
        )

        return ResponseEntity.ok(result)
    }

    @GetMapping("/search/count")
    fun getTransactionsCount(
        @RequestParam userId: Int,
        @RequestParam startDate: String,
        @RequestParam endDate: String,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) categoryId: Int?,
        @RequestParam(required = false) isRecurring: Boolean?,
        @RequestParam(required = false) hasInstallments: Boolean?,
        @RequestParam(required = false) description: String?,
        @RequestParam(required = false) minAmount: Int?,
        @RequestParam(required = false) maxAmount: Int?
    ): ResponseEntity<Map<String, Long>> {

        val filter = SearchTransactionFilter(
            userId = userId,
            startDate = LocalDate.parse(startDate),
            endDate = LocalDate.parse(endDate),
            type = type?.let { TransactionType.valueOf(it.uppercase()) },
            status = status?.let { PaymentStatus.valueOf(it.uppercase()) },
            categoryId = categoryId,
            isRecurring = isRecurring,
            hasInstallments = hasInstallments,
            description = description,
            minAmount = minAmount,
            maxAmount = maxAmount
        )

        val count = searchTransactionService.searchTransactionsCount(filter)
        return ResponseEntity.ok(mapOf("totalCount" to count))
    }
}
