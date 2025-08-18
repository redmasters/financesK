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
        AND (
        (t.dueDate BETWEEN :startDate AND :endDate) OR
        (t.status = 'PAID' AND t.paidAt BETWEEN :startDate AND :endDate)
        )
        AND (:status IS NULL OR t.status = :status)
        AND (:type IS NULL OR t.type = :type)
        AND (:categoryId IS NULL OR 
             t.categoryId.id = :categoryId OR 
             t.categoryId.id IN (SELECT c.id FROM Category c WHERE c.parent.id = :categoryId))
        AND (:isRecurring IS NULL OR 
             (:isRecurring = true AND t.recurrencePattern IS NOT NULL) OR
             (:isRecurring = false AND t.recurrencePattern IS NULL))
        AND (:hasInstallments IS NULL OR 
             (:hasInstallments = true AND t.installmentInfo IS NOT NULL) OR
             (:hasInstallments = false AND t.installmentInfo IS NULL))
        AND (:description IS NULL OR t.description LIKE :description)
        AND (:minAmount IS NULL OR t.amount >= :minAmount)
        AND (:maxAmount IS NULL OR t.amount <= :maxAmount)
        and 
        (:accountsId IS NULL OR t.accountId.accountId IN :accountsId)
        """
    )
    fun getIncomeExpenseBalance(
        @Param("userId") userId: Int,
        @Param("accountsId") accountsId: List<Int>?,
        @Param("status") status: PaymentStatus?,
        @Param("type") type: TransactionType?,
        @Param("categoryId") categoryId: Int?,
        @Param("isRecurring") isRecurring: Boolean?,
        @Param("hasInstallments") hasInstallments: Boolean?,
        @Param("description") description: String?,
        @Param("minAmount") minAmount: Int?,
        @Param("maxAmount") maxAmount: Int?,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): AmountIncomeExpenseResponse

    @Query(
        """
        SELECT t FROM Transaction t
        WHERE t.userId.id = :userId
        AND(
        (t.dueDate BETWEEN :startDate AND :endDate) OR
        (t.status = 'PAID' AND t.paidAt BETWEEN :startDate AND :endDate)
        )
        AND (:type IS NULL OR t.type = :type)
        AND (:status IS NULL OR t.status = :status)
        AND (:categoryId IS NULL OR 
             t.categoryId.id = :categoryId OR 
             t.categoryId.id IN (SELECT c.id FROM Category c WHERE c.parent.id = :categoryId))
        AND (:isRecurring IS NULL OR 
             (:isRecurring = true AND t.recurrencePattern IS NOT NULL) OR
             (:isRecurring = false AND t.recurrencePattern IS NULL))
        AND (:hasInstallments IS NULL OR 
             (:hasInstallments = true AND t.installmentInfo IS NOT NULL) OR
             (:hasInstallments = false AND t.installmentInfo IS NULL))
        AND (:description IS NULL OR t.description LIKE :description)
        AND (:minAmount IS NULL OR t.amount >= :minAmount)
        AND (:maxAmount IS NULL OR t.amount <= :maxAmount)
        and 
        (:accountsId IS NULL OR t.accountId.accountId IN :accountsId)
        """
    )
    fun findTransactionsByFilters(
        @Param("userId") userId: Int,
        @Param("accountsId") accountsId: List<Int>?,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        @Param("type") type: TransactionType?,
        @Param("status") status: PaymentStatus?,
        @Param("categoryId") categoryId: Int?,
        @Param("isRecurring") isRecurring: Boolean?,
        @Param("hasInstallments") hasInstallments: Boolean?,
        @Param("description") description: String?,
        @Param("minAmount") minAmount: Int?,
        @Param("maxAmount") maxAmount: Int?,
        pageable: Pageable
    ): Page<Transaction>

    @Query(
        """
        SELECT COUNT(t) FROM Transaction t
        WHERE t.userId.id = :userId
        AND t.dueDate BETWEEN :startDate AND :endDate
        AND (:type IS NULL OR t.type = :type)
        AND (:status IS NULL OR t.status = :status)
        AND (:categoryId IS NULL OR 
             t.categoryId.id = :categoryId OR 
             t.categoryId.id IN (SELECT c.id FROM Category c WHERE c.parent.id = :categoryId))
        AND (:isRecurring IS NULL OR 
             (:isRecurring = true AND t.recurrencePattern IS NOT NULL) OR
             (:isRecurring = false AND t.recurrencePattern IS NULL))
        AND (:hasInstallments IS NULL OR 
             (:hasInstallments = true AND t.installmentInfo IS NOT NULL) OR
             (:hasInstallments = false AND t.installmentInfo IS NULL))
        AND (:description IS NULL OR t.description LIKE :description)
        AND (:minAmount IS NULL OR t.amount >= :minAmount)
        AND (:maxAmount IS NULL OR t.amount <= :maxAmount)
        """
    )
    fun countTransactionsByFilters(
        @Param("userId") userId: Int,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        @Param("type") type: TransactionType?,
        @Param("status") status: PaymentStatus?,
        @Param("categoryId") categoryId: Int?,
        @Param("isRecurring") isRecurring: Boolean?,
        @Param("hasInstallments") hasInstallments: Boolean?,
        @Param("description") description: String?,
        @Param("minAmount") minAmount: Int?,
        @Param("maxAmount") maxAmount: Int?
    ): Long
}
