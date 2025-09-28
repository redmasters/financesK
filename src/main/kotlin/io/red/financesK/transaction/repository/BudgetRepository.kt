package io.red.financesK.transaction.repository

import io.red.financesK.transaction.model.Budget
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate

@Repository
interface BudgetRepository : JpaRepository<Budget, Int> {

    fun findByUserId(userId: Int): List<Budget>

    fun findByUserIdAndCategoryId(userId: Int, categoryId: Int?): List<Budget>

    fun findByUserIdAndMonth(userId: Int, month: LocalDate): List<Budget>

    fun findByUserIdAndMonthBetween(userId: Int, startMonth: LocalDate, endMonth: LocalDate): List<Budget>

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND EXTRACT(YEAR FROM b.month) = :year")
    fun findByUserIdAndYear(@Param("userId") userId: Int, @Param("year") year: Int): List<Budget>

    @Query("SELECT SUM(b.amount) FROM Budget b WHERE b.user.id = :userId AND b.month = :month")
    fun sumAmountByUserIdAndMonth(@Param("userId") userId: Int, @Param("month") month: LocalDate): BigDecimal?

    fun existsByUserIdAndCategoryIdAndMonth(userId: Int, categoryId: Int?, month: LocalDate): Boolean
}
