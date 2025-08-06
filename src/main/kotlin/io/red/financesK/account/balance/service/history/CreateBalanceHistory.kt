package io.red.financesK.account.balance.service.history

import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.account.balance.model.AccountBalanceHistory
import io.red.financesK.account.balance.repository.AccountBalanceHistoryRepository
import io.red.financesK.account.service.search.SearchAccountService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class CreateBalanceHistory(
    private val accountBalanceHistoryRepository: AccountBalanceHistoryRepository,
    private val searchAccountService: SearchAccountService
) {
    private val log = LoggerFactory.getLogger(CreateBalanceHistory::class.java)

    fun createBalanceHistory(accountId: Int?, amount: BigDecimal, operationType: AccountOperationType) {
        val account = searchAccountService.searchAccountById(accountId)
        require(account.accountId != null) { "Account with id $accountId not found" }

        val balanceHistory = AccountBalanceHistory(
            account = account,
            amount = amount,
            operationType = AccountOperationType.fromString(operationType.toString())
        )
        accountBalanceHistoryRepository.save(balanceHistory)
        log.info(
            "m='createBalanceHistory', action='balance history created successfully', accountId='{}', amount='{}', operationType='{}'",
            accountId, amount, operationType
        )
    }
}
