package io.red.financesk.services

import io.red.financesk.models.Transaction
import io.red.financesk.repositories.TransactionJpaRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SearchTransaction(
  private val transactionJpaRepository: TransactionJpaRepository
) {

  fun searchAllByDate(date: LocalDate): List<Transaction> {
    val transactions = transactionJpaRepository.findAllByDataLancamento(date) ?:
    throw Exception("No transactions found for this date")

    

  }
}
