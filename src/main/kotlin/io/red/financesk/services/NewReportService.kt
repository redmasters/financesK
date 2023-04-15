package io.red.financesk.services

import io.red.financesk.repositories.TransactionJpaRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Path

const val LOCATION = "src/main/resources/"
const val FILE_NAME = "input_tmp.csv"
var log: Logger = LoggerFactory.getLogger(NewReportService::class.java)

@Service
class NewReportService(
  transactionJpaRepository: TransactionJpaRepository
) {
  fun saveNewReportFile(multipartFile: MultipartFile) {
    log.info("Saving new report file")
    multipartFile.transferTo(Path.of(LOCATION+ FILE_NAME))
  }
}
