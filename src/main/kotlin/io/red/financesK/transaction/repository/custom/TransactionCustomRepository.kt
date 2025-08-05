package io.red.financesK.transaction.repository.custom

import com.fasterxml.jackson.databind.ObjectMapper
import io.red.financesK.transaction.model.Transaction
import io.red.financesK.transaction.controller.request.SearchTransactionFilter
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.RecurrencePattern
import io.red.financesK.transaction.enums.TransactionType
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

        // Filtros básicos
        filter.description?.let {
            filterSql.append(" AND t.description LIKE :description")
            params["description"] = "%$it%"
        }
        filter.amount?.let {
            filterSql.append(" AND t.amount = :amount")
            params["amount"] = it
        }
        filter.downPayment?.let {
            filterSql.append(" AND t.down_payment = :downPayment")
            params["downPayment"] = it
        }
        filter.type?.let {
            filterSql.append(" AND t.type = :type")
            params["type"] = it
        }
        filter.categoryId?.let {
            filterSql.append(" AND t.category_id = :categoryId")
            params["categoryId"] = it
        }
        filter.status?.let {
            filterSql.append(" AND t.payment_status = :status")
            params["status"] = it
        }
        filter.dueDate?.let {
            filterSql.append(" AND t.due_date = :dueDate")
            params["dueDate"] = it
        }
        filter.notes?.let {
            filterSql.append(" AND t.notes LIKE :notes")
            params["notes"] = "%$it%"
        }
        filter.recurrencePattern?.let {
            filterSql.append(" AND t.recurrence_pattern = :recurrencePattern")
            params["recurrencePattern"] = it
        }
        filter.totalInstallments?.let {
            filterSql.append(" AND t.installment_info->>'totalInstallments' = :totalInstallments")
            params["totalInstallments"] = it.toString()
        }
        filter.userId?.let {
            filterSql.append(" AND t.user_id = :userId")
            params["userId"] = it
        }

        // Filtros por período de data
        filter.startDate?.let {
            filterSql.append(" AND t.due_date >= :startDate")
            params["startDate"] = it
        }
        filter.endDate?.let {
            filterSql.append(" AND t.due_date <= :endDate")
            params["endDate"] = it
        }

        // Filtros avançados por valor
        filter.minAmount?.let {
            filterSql.append(" AND t.amount >= :minAmount")
            params["minAmount"] = it
        }
        filter.maxAmount?.let {
            filterSql.append(" AND t.amount <= :maxAmount")
            params["maxAmount"] = it
        }

        // Filtro por entrada (down payment)
        filter.hasDownPayment?.let { hasDownPayment ->
            if (hasDownPayment) {
                filterSql.append(" AND t.down_payment IS NOT NULL AND t.down_payment > 0")
            } else {
                filterSql.append(" AND (t.down_payment IS NULL OR t.down_payment = 0)")
            }
        }

        // Filtro por parcelamento
        filter.isInstallment?.let { isInstallment ->
            if (isInstallment) {
                filterSql.append(" AND t.installment_info IS NOT NULL AND CAST(t.installment_info->>'totalInstallments' AS INTEGER) > 1")
            } else {
                filterSql.append(" AND (t.installment_info IS NULL OR CAST(t.installment_info->>'totalInstallments' AS INTEGER) <= 1)")
            }
        }

        // Filtro por nome da categoria (JOIN necessário)
        val needsCategoryJoin = filter.categoryName != null
        filter.categoryName?.let {
            filterSql.append(" AND c.name LIKE :categoryName")
            params["categoryName"] = "%$it%"
        }

        // Filtros por período pré-definido
        val now = java.time.LocalDate.now()
        filter.currentMonth?.let { currentMonth ->
            if (currentMonth) {
                val startOfMonth = now.withDayOfMonth(1)
                val endOfMonth = now.withDayOfMonth(now.lengthOfMonth())
                filterSql.append(" AND t.due_date >= :currentMonthStart AND t.due_date <= :currentMonthEnd")
                params["currentMonthStart"] = startOfMonth
                params["currentMonthEnd"] = endOfMonth
            }
        }

        filter.currentWeek?.let { currentWeek ->
            if (currentWeek) {
                val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                val endOfWeek = startOfWeek.plusDays(6)
                filterSql.append(" AND t.due_date >= :currentWeekStart AND t.due_date <= :currentWeekEnd")
                params["currentWeekStart"] = startOfWeek
                params["currentWeekEnd"] = endOfWeek
            }
        }

        filter.currentYear?.let { currentYear ->
            if (currentYear) {
                val startOfYear = now.withDayOfYear(1)
                val endOfYear = now.withDayOfYear(now.lengthOfYear())
                filterSql.append(" AND t.due_date >= :currentYearStart AND t.due_date <= :currentYearEnd")
                params["currentYearStart"] = startOfYear
                params["currentYearEnd"] = endOfYear
            }
        }

        // Construir a query com JOIN se necessário
        val baseQuery = if (needsCategoryJoin) {
            "SELECT t.* FROM tbl_transaction t INNER JOIN tbl_category c ON t.id = c.id WHERE 1=1"
        } else {
            "SELECT t.* FROM tbl_transaction t WHERE 1=1"
        }

        val sql = StringBuilder(baseQuery)
        sql.append(filterSql)
        sql.append(" ORDER BY t.due_date, t.created_at")
        sql.append(" LIMIT :limit OFFSET :offset")
        params["limit"] = pageable.pageSize
        params["offset"] = pageable.offset

        // RowMapper atualizado para usar aliases da tabela
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
                null
            )

            val installmentInfo = rs.getString("installment_info")?.let {
                objectMapper.readValue(it, InstallmentInfo::class.java)
            }

            Transaction(
                id = rs.getInt("id"),
                description = rs.getString("description"),
                amount = rs.getBigDecimal("amount"),
                downPayment = rs.getBigDecimal("down_payment"),
                type = rs.getString("type")?.let { TransactionType.fromString(it) },
                status = rs.getString("payment_status")?.let { PaymentStatus.valueOf(it) } ?: PaymentStatus.PENDING,
                categoryId = category,
                dueDate = rs.getDate("due_date").toLocalDate(),
                createdAt = rs.getTimestamp("created_at")?.toInstant(),
                userId = userId,
                installmentInfo = installmentInfo,
                notes = rs.getString("notes"),
                recurrencePattern = rs.getString("recurrence_pattern")?.let { RecurrencePattern.fromString(it) }
            )
        }

        log.info("m='findByDynamicFilter', acao='executando consulta SQL', sql='{}'", sql)
        val transactions = jdbcTemplate.query(sql.toString(), params, rowMapper)

        // Para total de elementos (COUNT com os mesmos filtros)
        val countBaseQuery = if (needsCategoryJoin) {
            "SELECT COUNT(*) FROM tbl_transaction t INNER JOIN tbl_category c ON t.category_id = c.id WHERE 1=1"
        } else {
            "SELECT COUNT(*) FROM tbl_transaction t WHERE 1=1"
        }

        val countSql = StringBuilder(countBaseQuery)
        countSql.append(filterSql)
        val total = jdbcTemplate.queryForObject(countSql.toString(), params, Int::class.java) ?: 0

        return PageImpl(transactions, pageable, total.toLong())
    }
}
