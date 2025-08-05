package io.red.financesK.transaction.repository.custom

import com.fasterxml.jackson.databind.ObjectMapper
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.controller.request.SearchTransactionFilter
import io.red.financesK.transaction.model.Category
import io.red.financesK.transaction.model.InstallmentInfo
import io.red.financesK.user.model.AppUser
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.HashMap

@Repository
class TransactionCustomRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(TransactionCustomRepository::class.java)

    fun findByDynamicFilter(filter: SearchTransactionFilter, pageable: Pageable): Page<Transaction> {
        log.info(
            "m='findByDynamicFilter', acao='buscando transações com filtros', filtro='{}', page={}, size={}",
            filter,
            pageable.pageNumber,
            pageable.pageSize
        )
        val filterSql = StringBuilder()
        val params = HashMap<String, Any?>()

        filter.description?.let {
            filterSql.append(" AND description LIKE :description")
            params["description"] = "%$it%"
        }
        filter.amount?.let {
            filterSql.append(" AND amount = :amount")
            params["amount"] = it
        }
        filter.type?.let {
            filterSql.append(" AND type = :type")
            params["type"] = it
        }
        filter.categoryId?.let {
            filterSql.append(" AND category_id = :categoryId")
            params["categoryId"] = it
        }
        filter.startDate?.let {
            filterSql.append(" AND transaction_date = :startDate")
            params["startDate"] = it
        }
        filter.notes?.let {
            filterSql.append(" AND notes LIKE :notes")
            params["notes"] = "%$it%"
        }
        filter.recurrencePattern?.let {
            filterSql.append(" AND recurrence_pattern = :recurrencePattern")
            params["recurrencePattern"] = it
        }
        filter.totalInstallments?.let {
            filterSql.append(" AND installment_info->>'totalInstallments' = :totalInstallments")
            params["totalInstallments"] = it
        }
        filter.userId?.let {
            filterSql.append(" AND user_id = :userId")
            params["userId"] = it
        }
        // Paginação
        val sql = StringBuilder("SELECT * FROM tbl_transaction WHERE 1=1")
        sql.append(filterSql)
        sql.append(" LIMIT :limit OFFSET :offset")
        params["limit"] = pageable.pageSize
        params["offset"] = pageable.offset

        val rowMapper = RowMapper { rs: ResultSet, _: Int ->
            val category = Category(
                id = rs.getInt("category_id"),
                null,
                null
            )
            val userId = AppUser(
                id = rs.getObject("user_id") as? Int,
                null,
                null,
                null,
                null,

            )

            val installmentInfo = rs.getString("installment_info")?.let {
                objectMapper.readValue(it, InstallmentInfo::class.java)
            }

            Transaction(
                id = rs.getInt("id"),
                description = rs.getString("description"),
                amount = rs.getBigDecimal("amount"),
                type = rs.getString("type")?.let { Transaction.TransactionType.fromString(it) },
                categoryId = category,
                transactionDate = rs.getDate("transaction_date").toLocalDate(),
                createdAt = rs.getTimestamp("created_at").toInstant(),
                userId = userId,
                installmentInfo = installmentInfo,
                notes = rs.getString("notes"),
                recurrencePattern = rs.getString("recurrence_pattern")?.let { Transaction.RecurrencePattern.fromString(it) }
            )
        }

        log.info("m='findByDynamicFilter', acao='executando consulta SQL', sql='{}'", sql)
        val transactions = jdbcTemplate.query(sql.toString(), params, rowMapper)
        // Para total de elementos
        val countSql = StringBuilder("SELECT COUNT(*) FROM tbl_transaction WHERE 1=1")
        countSql.append(filterSql)
        val total = jdbcTemplate.queryForObject(countSql.toString(), params, Int::class.java) ?: 0
        return PageImpl(transactions, pageable, total.toLong())
    }
}
