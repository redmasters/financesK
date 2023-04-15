package io.red.financesk.repositories

import io.red.financesk.models.Transaction
import io.red.financesk.models.Transaction2023
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface Transaction2023JpaRepository : JpaRepository<Transaction2023, Long> {
  fun findAllByDataLancamento(date: LocalDate): List<Transaction2023>?

  @Query("select t from Transaction2023 t where t.historico in :list")
  fun findAllByHistoricoIn(list: List<Transaction2023>): List<Transaction2023>?
}
