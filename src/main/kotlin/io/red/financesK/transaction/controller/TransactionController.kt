package io.red.financesK.transaction.controller

import io.red.financesK.transaction.controller.request.CreateTransactionRequest
import io.red.financesK.transaction.controller.request.SearchTransactionFilter
import io.red.financesK.transaction.controller.response.TransactionResponse
import io.red.financesK.transaction.service.create.CreateTransactionService
import io.red.financesK.transaction.service.search.SearchTransactionService
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
    private val searchTransactionService: SearchTransactionService
) {
    @PostMapping
    fun create(@RequestBody request: CreateTransactionRequest): ResponseEntity.BodyBuilder {
        createTransactionService.execute(request)
        return ResponseEntity.ok()
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
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) categoryId: Int?,
        @RequestParam(required = false) startDate: LocalDate?,
        @RequestParam(required = false) notes: String?,
        @RequestParam(required = false) recurrencePattern: String?,
        @RequestParam(required = false) totalInstallments: Int?,
        @RequestParam(required = false) userId: Int?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<TransactionResponse>> {
        val filter = SearchTransactionFilter(
            description = description,
            amount = amount,
            type = type,
            categoryId = categoryId,
            startDate = startDate,
            notes = notes,
            recurrencePattern = recurrencePattern,
            totalInstallments = totalInstallments,
            userId = userId
        )
        val result = searchTransactionService.execute(filter, PageRequest.of(page, size))
        return ResponseEntity.ok(result)
    }
}
