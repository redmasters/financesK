package io.red.financesK.account.repository

import io.red.financesK.account.model.Account
import io.red.financesK.user.model.AppUser
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<Account, Int> {
    fun findAllByUserId(user: AppUser) : List<Account>
}
