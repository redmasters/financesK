package io.red.financesk.dtos

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

class TransactionDto(
  val dataLancamento: String,
  val historico: String,
  val valor: String,
  val saldo: String
)
