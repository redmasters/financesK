package io.red.financesk.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class FormatValuesUtilTest{

  @Test
  fun `should format value`() {
    val valueToFormat = "1.000,00"
    val value = FormatValuesUtil(valueToFormat).formatValue()
    print(value)
    assertEquals(1000.00, value)
  }

}
