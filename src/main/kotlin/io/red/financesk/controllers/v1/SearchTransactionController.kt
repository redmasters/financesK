package io.red.financesk.controllers.v1

import io.red.financesk.controllers.v1.mapping.FinancesKV1RestMapping
import io.red.financesk.models.Transaction
import io.red.financesk.services.SearchTransactionService
import org.springframework.web.bind.annotation.GetMapping
import java.time.LocalDate

@FinancesKV1RestMapping
class SearchTransactionController(
  private val searchTransactionService: SearchTransactionService
) {

  @GetMapping("/transactions")
  fun searchAllByDate(date: LocalDate): List<Transaction>{
    return searchTransactionService.searchAllByDate(date)
  }

}
