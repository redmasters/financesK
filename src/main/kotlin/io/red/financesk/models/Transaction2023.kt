package io.red.financesk.models

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "tbl_transactions_2023")
data class Transaction2023(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,
  val dataLancamento: LocalDate,
  val historico: String,
  val descricao: String,
  val valor: Double
)
