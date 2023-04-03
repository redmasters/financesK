package io.red.financesk.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class DateConverterUtilTest{

  @Test
  @DisplayName("Should convert date")
  fun `should convert date`() {
    val dateToConvert = "03/04/2020"
    val date = DateConverterUtil(dateToConvert).convertDate()
    print(date)
    assertThat(date).isEqualTo("2020-04-03")
  }

}

