package io.red.financesK.account.repository

import io.red.financesK.account.model.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : JpaRepository<Account, Int> {
    fun findByUserId_Id(userId: Int): List<Account>
    fun findByAccountIdAndUserId_Id(accountId: Int, userId: Int): Account?
    fun findByAccountNameContainingIgnoreCaseAndUserId_Id(accountName: String, userId: Int): List<Account>

    @Query("""
        SELECT a FROM Account a
        WHERE a.userId.id = :userId
    """)
    fun findAllByUserId(userId: Int): List<Account>
}
