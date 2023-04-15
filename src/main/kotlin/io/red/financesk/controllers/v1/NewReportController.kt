package io.red.financesk.controllers.v1

import io.red.financesk.controllers.v1.mapping.FinancesKV1RestMapping
import io.red.financesk.services.NewReportService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.multipart.MultipartFile

@FinancesKV1RestMapping
class NewReportController(
  private val newReportService: NewReportService
) {
  @PostMapping("/newReport")
  fun saveNewReportFile(@RequestBody file: MultipartFile): ResponseEntity<String>{
    newReportService.saveNewReportFile(file)
    return ResponseEntity.ok("File saved")
  }
}
