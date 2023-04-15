package io.red.financesk.services

import io.red.financesk.repositories.TransactionJpaRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.multipart.MultipartFile

@ExtendWith(MockitoExtension::class)
class NewReportServiceTest() {

  @Mock
  private lateinit var transactionJpaRepository: TransactionJpaRepository
  @InjectMocks
  private lateinit var newReportService: NewReportService

  @Spy
  lateinit var file: MultipartFile

  @Test
  fun `should save a new report file`() {
    val service = newReportService.saveNewReportFile(file)
    assertNotNull(service)
  }

}
