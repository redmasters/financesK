package io.red.financesK.account.balance.repository

import io.red.financesK.account.balance.model.AccountBalanceHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.LocalDate

interface AccountBalanceHistoryRepository : JpaRepository<AccountBalanceHistory, Int> {

    @Query("""
        SELECT COALESCE(SUM(bh.amount), 0) 
        FROM AccountBalanceHistory bh 
        INNER JOIN bh.account a 
        WHERE a.userId.id = :userId 
        AND DATE(bh.balanceTimestamp) BETWEEN :startDate AND :endDate 
        AND bh.account.accountId IN :accountIds
    """)
    fun getTotalBalanceByUserAndDateRangeAndAccounts(
        @Param("userId") userId: Int,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        @Param("accountIds") accountIds: List<Int>
    ): BigDecimal

//    @Query("""
//        SELECT new io.red.financesK.account.balance.controller.response.BalanceSummaryResponse(
//            COALESCE(SUM(bh.amount), 0),
//            MAX(bh.balanceTimestamp),
//            bh.transactionId.id
//        )
//        FROM AccountBalanceHistory bh
//        INNER JOIN bh.account a
//        WHERE a.userId.id = :userId
//        AND DATE(bh.balanceTimestamp) BETWEEN :startDate AND :endDate
//        AND bh.account.accountId IN :accountIds
//        group by bh.transactionId
//    """)
//    fun getTotalBalanceWithLastUpdateByUserAndDateRangeAndAccounts(
//        @Param("userId") userId: Int,
//        @Param("startDate") startDate: LocalDate,
//        @Param("endDate") endDate: LocalDate,
//        @Param("accountIds") accountIds: List<Int>
//    ): List<BalanceSummaryResponse>
}
