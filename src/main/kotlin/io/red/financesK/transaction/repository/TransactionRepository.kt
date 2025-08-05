package io.red.financesK.transaction.repository

import io.red.financesK.transaction.model.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : JpaRepository<Transaction, Int> {

}
