package io.red.financesK.account.balance.service.update

import io.red.financesK.account.service.search.SearchAccountService
import io.red.financesK.account.service.update.UpdateAccountService

class OperationBalanceService(
    private val updateAccountService: UpdateAccountService,
    private val searchAccountService: SearchAccountService
) {
    fun sumBalance(accountId: Int, amount: Double): Double {
        val account = searchAccountService.searchAccountById(accountId)
        val currentBalance = account.balance?.toDoubleOrNull() ?: 0.0
        val newBalance = currentBalance + amount
        updateAccountService.updateAccountBalance(accountId, newBalance.toString())
        return newBalance
    }

}
