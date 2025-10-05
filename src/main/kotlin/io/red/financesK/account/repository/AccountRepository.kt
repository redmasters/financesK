package io.red.financesK.account.repository

import io.red.financesK.account.model.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : JpaRepository<Account, Int> {

    @Query("""
        SELECT a FROM Account a
        WHERE a.userId.id = :userId
    """)
    fun findAllByUserId(userId: Int): List<Account>

    @Query("""
        SELECT COALESCE(SUM(a.accountCurrentBalance), 0)
        FROM Account a
        WHERE a.userId.id = :userId
        AND a.accountId IN :accountIds
    """)
    fun getTotalBalanceByUserIdAndAccountIds(userId: Int, accountIds: List<Int>): Int
}
