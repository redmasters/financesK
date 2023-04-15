package io.red.financesk.services

import io.red.financesk.models.Transaction
import io.red.financesk.repositories.TransactionJpaRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class SearchTransactionServiceTest {

  @Mock
  private lateinit var transactionJpaRepository: TransactionJpaRepository

  @InjectMocks
  lateinit var searchTransactionService: SearchTransactionService

  @Test
  fun `should return a list of transactions`() {
    val date = LocalDate.of(2020, 1, 1)
    val transaction = Transaction(
      id = 1L,
      dataLancamento = date,
      historico = "Test",
      valor = 100.0,
      saldo = 100.0
    )
    val transactionList = listOf(transaction)

    `when`(transactionJpaRepository.findAllByDataLancamento(date))
      .thenReturn(transactionList)

    val result = searchTransactionService.searchAllByDate(date)

    assertEquals(transactionList, result)
  }

}

