package io.red.financesk.controllers.v1

import io.red.financesk.controllers.v1.mapping.FinancesKV1RestMapping
import io.red.financesk.services.SaveReportService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus

@FinancesKV1RestMapping
class SaveTransactionController(
  private val saveReportService: SaveReportService
) {
  @PostMapping("/process-report")
  @ResponseStatus(CREATED)
  fun saveReport() {
    return saveReportService.processReportData()
  }
}
