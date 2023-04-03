package io.red.financesk.services

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import io.red.financesk.models.Transaction
import io.red.financesk.repositories.TransactionJpaRepository
import io.red.financesk.utils.DateConverterUtil
import io.red.financesk.utils.FormatValuesUtil
import org.springframework.stereotype.Service
import java.io.File

const val INPUT_FILE = "src/main/resources/data.csv"
const val OUTPUT_FILE = "src/main/resources/output.csv"

@Service
class SaveReportService(
  private val transactionJpaRepository: TransactionJpaRepository
) {

  fun processReportData() {
    val reportList = mutableListOf<List<String>>()
    val rowsToSkip = 4

    File(INPUT_FILE).useLines { lines ->
      lines.drop(rowsToSkip)
        .filter { it.isNotBlank() }
        .forEach { line ->
          val cells = line.split(";")
          val rows = listOf(
            cells[0],
            cells[1],
            cells[2],
            cells[3]
          )
          reportList.add(rows)
        }
    }

    csvWriter().open(OUTPUT_FILE) {
      writeRows(reportList)
    }

    readSavedReport()

  }

 fun readSavedReport() {
   val transactionList = mutableListOf<Transaction>()

    csvReader().open(OUTPUT_FILE) {
      readAllWithHeaderAsSequence().forEach { row ->
        val formattedDate = DateConverterUtil(row["DATA LANÇAMENTO"] ?: "")
          .convertDate()
        val formattedValue = FormatValuesUtil(row["VALOR"] ?: "")
          .formatValue()
        val formattedBalance = FormatValuesUtil(row["SALDO"] ?: "")
          .formatValue()

        val transactionEntity = Transaction(
          dataLancamento = formattedDate,
          historico = row["HISTÓRICO"] ?: "",
          valor = formattedValue,
          saldo = formattedBalance
        )

        transactionList.add(transactionEntity)
      }
    }

    transactionJpaRepository.saveAll(transactionList)

  }
}




