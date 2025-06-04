package io.red.financesK.spending.repository

import io.red.financesK.spending.controller.request.FilterSpendRequest
import io.red.financesK.spending.enums.SpendStatus
import io.red.financesK.spending.model.Spend
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class SpendCustomRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val filterSpendCustomQuery: FilterSpendCustomQuery,
    private val spendCategoryRepository: SpendCategoryRepository
) {
    fun filterSpendBy(request: FilterSpendRequest): List<Spend> {
        val query = filterSpendCustomQuery.filterSpendQueryBy(request)
        return jdbcTemplate.query(query) { rs, _ ->
            Spend(
                id = rs.getLong("spend_id"),
                name = rs.getString("name"),
                description = rs.getString("description"),
                amount = rs.getBigDecimal("amount"),
                dueDate = rs.getDate("due_date").toLocalDate(),
                category = rs.getLong("category_id").let { categoryId ->
                    spendCategoryRepository.findById(categoryId).orElseThrow {
                        IllegalArgumentException("Category with id $categoryId not found")
                    }
                },
                isPaid = rs.getBoolean("is_paid"),
                isDue = rs.getBoolean("is_due"),
                isRecurring = rs.getBoolean("is_recurring"),
                status = SpendStatus.valueOf(rs.getString("status"))
            )
        }
    }

}

