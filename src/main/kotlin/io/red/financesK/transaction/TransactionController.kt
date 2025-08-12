package io.red.financesK.transaction

import io.red.financesK.transaction.controller.request.CreateTransactionRequest
import io.red.financesK.transaction.controller.response.AmountIncomeExpenseResponse
import io.red.financesK.transaction.controller.response.CreateTransactionResponse
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.service.create.CreateTransactionService
import io.red.financesK.transaction.service.search.SearchTransactionService
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
}
