package io.red.financesK.account.repository

import io.red.financesK.account.balance.controller.response.BalanceSummaryResponse
import io.red.financesK.account.model.Account
import io.red.financesK.user.model.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface AccountRepository : JpaRepository<Account, Int> {
    fun findAllByUserId(user: AppUser) : List<Account>

    @Query("""
        select new io.red.financesK.account.balance.controller.response.BalanceSummaryResponse(
            COALESCE(SUM(account.accountInitialBalance), 0), 
            MAX(account.updatedAt)
        )
        from Account as account
        where account.userId.id = :userId
        and account.accountId in :accountIds
        and DATE(account.updatedAt) between :startDate and :endDate
    """)
    fun getTotalBalanceWithLastUpdateByUserAndDateRangeAndAccounts(
        @Param("userId") userId: Int,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        @Param("accountIds") accountIds: List<Int>
    ): BalanceSummaryResponse
}
