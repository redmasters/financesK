package io.red.financesK.account.balance.repository

import io.red.financesK.account.balance.model.AccountBalanceHistory
import org.springframework.data.jpa.repository.JpaRepository

interface AccountBalanceHistoryRepository : JpaRepository<AccountBalanceHistory, Int> {
}
