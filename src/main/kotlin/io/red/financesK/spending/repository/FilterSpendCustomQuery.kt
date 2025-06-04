package io.red.financesK.spending.repository

import io.red.financesK.spending.controller.request.FilterSpendRequest
import org.springframework.stereotype.Component

@Component
class FilterSpendCustomQuery {
    fun filterSpendQueryBy(
        request: FilterSpendRequest
    ): String {
        val params = request.toQueryParams()
        val startDate = params["startDate"] as String
        val endDate = params["endDate"] as String
        val isPaid = params["isPaid"] as Boolean
        val isDue = params["isDue"] as Boolean
        val categoryId = params["categoryId"] as Long

        if (!isPaid && !isDue && categoryId == 0L) {
            return """
                SELECT * FROM finances_spend
                WHERE due_date >= '$startDate' AND due_date <= '$endDate';
            """.trimIndent()
        }

        val conditions = mutableListOf<String>()

        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            conditions.add("due_date >= '$startDate' AND due_date <= '$endDate'")
        }

        if(isPaid && isDue) {
            conditions.add("is_paid = true AND is_due = true")
        } else if (isPaid) {
            conditions.add("is_paid = true")
        } else if (isDue) {
            conditions.add("is_due = true")
        }

        if (categoryId > 0) {
            conditions.add("category_id = $categoryId")
        }

        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE " + conditions.joinToString(" AND ")
        } else {
            ""
        }
        return """
            SELECT * FROM finances_spend
            $whereClause;
        """.trimIndent()

    }
}
