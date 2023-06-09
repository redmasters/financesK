package io.red.financesk.services

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import io.red.financesk.models.Transaction2023
import io.red.financesk.repositories.Transaction2023JpaRepository
import io.red.financesk.repositories.TransactionJpaRepository
import io.red.financesk.utils.DateConverterUtil
import io.red.financesk.utils.FormatValuesUtil
import org.springframework.stereotype.Service
import java.io.File

const val INPUT_FILE = "src/main/resources/input_tmp.csv"
const val OUTPUT_FILE = "src/main/resources/output.csv"

@Service
class SaveReportService(
  private val transactionJpaRepository: TransactionJpaRepository,
  private val transaction2023JpaRepository: Transaction2023JpaRepository
) {

  fun processReportData() {
    val reportList = mutableListOf<List<String>>()
    val rowsToSkip = 4

    File(INPUT_FILE).useLines { lines ->
      lines.drop(rowsToSkip)
        .filter { it.isNotBlank() }
        .map { it.lowercase() }
        .forEach { line ->
          val cells = line.split(";")
          val rows = listOf(
            cells[0],
            cells[1],
            cells[2],
            cells[3],
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
    val transactionList = mutableListOf<Transaction2023>()

    csvReader().open(OUTPUT_FILE) {
      readAllWithHeaderAsSequence().forEach { row ->
        val formattedDate = DateConverterUtil(row["data lançamento"] ?: "")
          .convertDate()
        val formattedValue = FormatValuesUtil(row["valor"] ?: "")
          .formatValue()

        val transactionEntity = Transaction2023(
          dataLancamento = formattedDate,
          historico = row["histórico"] ?: "",
          descricao = row["descrição"] ?: "",
          valor = formattedValue
        )

        transactionList.add(transactionEntity)
      }
    }

    verifyTransactionList(transactionList)

    transaction2023JpaRepository.saveAll(transactionList)
  }

  private fun verifyTransactionList(transactionList: MutableList<Transaction2023>): String {
    val findTransaction = transaction2023JpaRepository
      .findAllByHistoricoIn(transactionList)
      ?: throw Exception("No transactions found for this date")

    if (findTransaction.isNotEmpty()) {
      throw Exception("Transactions already saved")
    }

    return "Test"
  }
}




