package io.red.financesk.repositories

import io.red.financesk.models.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TransactionJpaRepository : JpaRepository<Transaction, Long> {
  fun findAllByDataLancamento(date: LocalDate): List<Transaction>?

  @Query("select t from Transaction t where t.historico in :list")
  fun findAllByHistoricoIn(list: List<Transaction>): List<Transaction>?
}
