package io.red.financesK.account.balance.service.update

import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.account.balance.service.history.CreateBalanceHistory
import io.red.financesK.account.service.search.SearchAccountService
import io.red.financesK.account.service.update.UpdateAccountService
import io.red.financesK.transaction.model.Transaction
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class OperationBalanceService(
    private val updateAccountService: UpdateAccountService,
    private val searchAccountService: SearchAccountService,
    private val createBalanceHistory: CreateBalanceHistory
) {
    fun sumBalance(accountId: Int?, amount: BigDecimal, operationType: AccountOperationType?,
                   transaction: Transaction): BigDecimal {
        val account = searchAccountService.searchAccountById(accountId)
        val currentBalance = account.accountInitialBalance
        val newBalance = currentBalance?.add(amount)
            ?: throw IllegalArgumentException("Current balance is null for account with id $accountId")
        updateAccountService.updateAccountBalance(accountId, newBalance.toString())

        createBalanceHistory.createBalanceHistory(
            accountId = accountId,
            amount = amount,
            operationType = operationType!!,
            transactionId = transaction
        )

        return newBalance
    }

    fun subtractBalance(accountId: Int?, amount: BigDecimal, operationType: AccountOperationType?,
                        transaction: Transaction): BigDecimal {
        val account = searchAccountService.searchAccountById(accountId)
        val currentBalance = account.accountInitialBalance
        val newBalance = currentBalance?.subtract(amount)
            ?: throw IllegalArgumentException("Current balance is null for account with id $accountId")
        updateAccountService.updateAccountBalance(account.accountId, newBalance.toString())

        createBalanceHistory.createBalanceHistory(
            accountId = account.accountId,
            amount = amount,
            operationType = operationType!!,
            transactionId = transaction

        )

        return newBalance
    }

}
