package io.red.financesK.spending.repository

import io.red.financesK.spending.controller.request.FilterSpendRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FilterSpendCustomQuery(
    private val logger: Logger = LoggerFactory.getLogger(FilterSpendCustomQuery::class.java)
) {
    fun filterSpendQueryBy(
        request: FilterSpendRequest
    ): String {
        val params = request.toQueryParams()
        val startDate = params["startDate"] as String
        val endDate = params["endDate"] as String
        val isPaid = params["isPaid"] as Boolean?
        val isDue = params["isDue"] as Boolean?
        val categoryId = params["categoryId"] as Long?

        if ((isPaid == null) && (isDue == null) && (categoryId == 0L)) {
            val query = """
                SELECT * FROM finances_spend
                WHERE due_date >= '$startDate' AND due_date <= '$endDate'
                ORDER BY amount;
            """.trimIndent()
            logger.info("m=filterSpendQueryBy, query: $query")
            return query
        }

        val conditions = mutableListOf<String>()

        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            conditions.add("due_date >= '$startDate' AND due_date <= '$endDate'")
        }

        if (isPaid != null) {
            conditions.add("is_paid = $isPaid")
        }
        if (isDue != null) {
            conditions.add("is_due = $isDue")
        }

        if ((categoryId != null) && (categoryId > 0)) {
            conditions.add("category_id = $categoryId")
        }

        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE " + conditions.joinToString(" AND ")
        } else {
            ""
        }
        val query = """
            SELECT * FROM finances_spend as spend
            $whereClause;
        """.trimIndent()

        logger.info("m=filterSpendQueryBy, query: $query")
        return query

    }
}
