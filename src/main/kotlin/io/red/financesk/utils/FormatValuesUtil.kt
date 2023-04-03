package io.red.financesk.utils

class FormatValuesUtil(val valueToFormat: String) {
  fun formatValue(): Double{
    val value = valueToFormat.replace(".", "")
    val replaced = value.replace(",", ".")
    return replaced.toDouble()
  }
}
