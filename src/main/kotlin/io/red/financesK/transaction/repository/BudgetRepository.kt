package io.red.financesK.transaction.repository

import io.red.financesK.transaction.model.Budget
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BudgetRepository : JpaRepository<Budget, Int>

