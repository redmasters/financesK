package io.red.financesk.repositories

import io.red.financesk.models.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionJpaRepository: JpaRepository<Transaction, Long>{
}
