package io.red.financesk.services

import io.red.financesk.repositories.TransactionJpaRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class SaveTransactionServiceTest {

  @Mock
  lateinit var transactionJpaRepository: TransactionJpaRepository

  @InjectMocks
  lateinit var saveReportService: SaveReportService

  @Test
  @DisplayName("Should print csv data")
  fun `should print csv data`() {

    val service = this.saveReportService.processReportData()
    Assertions.assertThat(service).isNotNull
  }

  @Test
  @DisplayName("Should read csv data")
  fun `should read csv data`() {
    Mockito.`when`(transactionJpaRepository.saveAll(anyList()))
      .thenReturn(listOf())
    val service = this.saveReportService.readSavedReport()
    Assertions.assertThat(service).isNotNull
  }

}
