package io.red.financesk.utils

import java.time.LocalDate

class DateConverterUtil(dateToConvert: String) {

  private val dateToConvert = dateToConvert
  fun convertDate(): LocalDate {
    val date = dateToConvert.split("/")
    val day = date[0].toInt()
    val month = date[1].toInt()
    val year = date[2].toInt()

    return LocalDate.of(year, month, day)
  }
}
