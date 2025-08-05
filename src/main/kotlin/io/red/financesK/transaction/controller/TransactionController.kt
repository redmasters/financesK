package io.red.financesK.transaction.controller

import io.red.financesK.transaction.controller.request.CreateTransactionRequest
import io.red.financesK.transaction.controller.request.SearchTransactionFilter
import io.red.financesK.transaction.controller.request.UpdateTransactionRequest
import io.red.financesK.transaction.controller.response.TransactionResponse
import io.red.financesK.transaction.service.create.CreateTransactionService
import io.red.financesK.transaction.service.search.SearchTransactionService
import io.red.financesK.transaction.service.update.UpdateTransactionService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate

@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val createTransactionService: CreateTransactionService,
    private val searchTransactionService: SearchTransactionService,
    private val updateTransactionService: UpdateTransactionService
) {
    @PostMapping
    fun create(@RequestBody request: CreateTransactionRequest): ResponseEntity<Void> {
        createTransactionService.execute(request)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Int): ResponseEntity<TransactionResponse> {
        val response = searchTransactionService.searchById(id)
        return response?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/search")
    fun search(
        @RequestParam(required = false) description: String?,
        @RequestParam(required = false) amount: BigDecimal?,
        @RequestParam(required = false) downPayment: BigDecimal?,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) categoryId: Int?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) startDate: LocalDate?,
        @RequestParam(required = false) endDate: LocalDate?,
        @RequestParam(required = false) dueDate: LocalDate?,
        @RequestParam(required = false) notes: String?,
        @RequestParam(required = false) recurrencePattern: String?,
        @RequestParam(required = false) totalInstallments: Int?,
        @RequestParam(required = false) userId: Int?,
        // Filtros avançados
        @RequestParam(required = false) hasDownPayment: Boolean?,
        @RequestParam(required = false) isInstallment: Boolean?,
        @RequestParam(required = false) categoryName: String?,
        @RequestParam(required = false) minAmount: BigDecimal?,
        @RequestParam(required = false) maxAmount: BigDecimal?,
        @RequestParam(required = false) currentMonth: Boolean?,
        @RequestParam(required = false) currentWeek: Boolean?,
        @RequestParam(required = false) currentYear: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<TransactionResponse>> {
        val filter = SearchTransactionFilter(
            description = description,
            amount = amount,
            downPayment = downPayment,
            type = type,
            categoryId = categoryId,
            status = status,
            startDate = startDate,
            endDate = endDate,
            dueDate = dueDate,
            notes = notes,
            recurrencePattern = recurrencePattern,
            totalInstallments = totalInstallments,
            userId = userId,
            hasDownPayment = hasDownPayment,
            isInstallment = isInstallment,
            categoryName = categoryName,
            minAmount = minAmount,
            maxAmount = maxAmount,
            currentMonth = currentMonth,
            currentWeek = currentWeek,
            currentYear = currentYear
        )
        val result = searchTransactionService.execute(filter, PageRequest.of(page, size))
        return ResponseEntity.ok(result)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody request: UpdateTransactionRequest
    ): ResponseEntity<Void> {
        updateTransactionService.execute(id, request)
        return ResponseEntity.ok().build()
    }
}
