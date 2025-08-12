package io.red.financesK.transaction.repository

import io.red.financesK.transaction.controller.response.AmountIncomeExpenseResponse
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.RecurrencePattern
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.model.Transaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TransactionRepository : JpaRepository<Transaction, Int> {

    fun findByUserId_IdOrderByDueDateDesc(userId: Int): List<Transaction>

    fun findByUserId_IdOrderByDueDateDesc(userId: Int, pageable: Pageable): Page<Transaction>

    fun findByUserId_IdAndType(userId: Int, type: TransactionType): List<Transaction>

    fun findByUserId_IdAndStatus(userId: Int, status: PaymentStatus): List<Transaction>

    fun findByUserId_IdAndCategoryId_Id(userId: Int, categoryId: Int): List<Transaction>

    fun findByUserId_IdAndDueDateBetween(userId: Int, startDate: LocalDate, endDate: LocalDate): List<Transaction>

    fun findByUserId_IdAndRecurrencePattern(userId: Int, recurrencePattern: RecurrencePattern): List<Transaction>

    @Query(
        "SELECT t FROM Transaction t " +
                "WHERE t.userId.id = :userId " +
                "AND t.dueDate BETWEEN :startDate AND :endDate " +
                "AND t.type = :type"
    )
    fun findByUserIdAndDateRangeAndType(
        @Param("userId") userId: Int,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        @Param("type") type: TransactionType
    ): List<Transaction>

    @Query(
        """
        SELECT new io.red.financesK.transaction.controller.response.AmountIncomeExpenseResponse(
            COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0)
        )
        FROM Transaction t
        WHERE t.userId.id = :userId
        AND (:status IS NULL OR t.status = :status)
        AND t.dueDate BETWEEN :startDate AND :endDate
        """
    )
    fun getIncomeExpenseBalance(
        @Param("userId") userId: Int,
        @Param("status") status: PaymentStatus?,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): AmountIncomeExpenseResponse

    @Query(
        "SELECT SUM(t.amount) FROM Transaction t " +
                "WHERE t.userId.id = :userId " +
                "AND t.type = :type " +
                "AND t.status = :status " +
                "AND t.dueDate BETWEEN :startDate AND :endDate"
    )
    fun sumAmountByUserIdAndTypeAndDateRange(
        @Param("userId") userId: Int,
        @Param("type") type: TransactionType?,
        @Param("status") status: PaymentStatus?,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): Int?
}
