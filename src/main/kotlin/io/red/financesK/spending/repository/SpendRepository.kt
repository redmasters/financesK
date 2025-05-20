package io.red.financesK.spending.repository

import io.red.financesK.spending.model.Spend
import org.springframework.data.jpa.repository.JpaRepository

interface SpendRepository : JpaRepository<Spend, Long> {
    fun findSpendByCategory_Id(categoryId: Long): List<Spend>
}
