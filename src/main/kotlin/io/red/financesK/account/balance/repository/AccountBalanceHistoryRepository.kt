package io.red.financesK.account.balance.repository

import io.red.financesK.account.balance.model.AccountBalanceHistory
import io.red.financesK.account.balance.enums.AccountOperationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant

@Repository
interface AccountBalanceHistoryRepository : JpaRepository<AccountBalanceHistory, Int> {

    fun findByAccount_AccountIdOrderByBalanceTimestampDesc(accountId: Int): List<AccountBalanceHistory>

    fun findByAccount_AccountIdAndOperationType(accountId: Int, operationType: AccountOperationType): List<AccountBalanceHistory>

    @Query("SELECT SUM(abh.amount) FROM AccountBalanceHistory abh " +
           "WHERE abh.account.accountId IN :accountIds " +
           "AND abh.balanceTimestamp BETWEEN :startDate AND :endDate " +
           "AND abh.account.userId.id = :userId")
    fun sumBalancesByAccountIdsAndDateRange(
        @Param("accountIds") accountIds: List<Int>,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant,
        @Param("userId") userId: Int
    ): BigDecimal?

    @Query("SELECT abh FROM AccountBalanceHistory abh " +
           "WHERE abh.account.userId.id = :userId " +
           "AND abh.balanceTimestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY abh.balanceTimestamp DESC")
    fun findByUserIdAndDateRange(
        @Param("userId") userId: Int,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<AccountBalanceHistory>
}
