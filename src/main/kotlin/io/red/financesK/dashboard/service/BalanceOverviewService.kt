package io.red.financesK.dashboard.service

import io.red.financesK.account.balance.service.search.SearchBalanceAccountService
import io.red.financesK.account.service.search.SearchAccountService
import io.red.financesK.dashboard.controller.response.BalanceProgress
import io.red.financesK.dashboard.controller.response.MonthlyBalance
import io.red.financesK.transaction.service.search.SearchTransactionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class BalanceOverviewService(
    private val searchAccountService: SearchAccountService,
    private val searchBalanceAccountService: SearchBalanceAccountService,
    private val searchTransactionService: SearchTransactionService
) {
    private val log = LoggerFactory.getLogger(BalanceOverviewService::class.java)

    fun getBalanceOverview(accountIds: List<Int>): BigDecimal {
        val balances = searchAccountService.getBalanceByAccountList(accountIds)
        log.info("m='getBalanceOverview', action='retrieving balances for accounts', accountIds='{}'", accountIds)
        if (balances.isEmpty()) {
            log.warn("m='getBalanceOverview', action='no balances found for provided account ids'")
            return BigDecimal.ZERO.setScale(2)
        }
        val totalBalance = balances
            .map { it.balance?.toBigDecimalOrNull() ?: BigDecimal.ZERO.setScale(2) }
            .reduce { acc, balance -> acc.add(balance) }
            .setScale(2)

        log.info(
            "m='getBalanceOverview', action='calculating total balance', accountIds='{}', totalBalance='{}'",
            accountIds, totalBalance
        )
        return totalBalance
    }

    fun getMonthlyBalanceOverview(
        accountIds: List<Int>,
        startDate: String,
        endDate: String,
        userId: Int
    ): MonthlyBalance {

        log.info(
            "m='getMonthlyBalanceOverview', action='retrieving monthly balance overview', accountIds='{}', startDate='{}', endDate='{}', userId='{}'",
            accountIds, startDate, endDate, userId
        )

        val balanceSummary = searchAccountService.getBalanceBy(
            userId, startDate, endDate, accountIds
        )
        log.info(
            "m='getMonthlyBalanceOverview', action='balance summary retrieved', balanceSummary='{}'",
            balanceSummary
        )
        return MonthlyBalance(
            value = balanceSummary.totalBalance,
            currency = "BRL",
            updatedAt = balanceSummary.lastUpdated.toString(),
            progress = BalanceProgress(
                monthElapsed = "38%",
                remainingBalance = "62%"
            )
        )


    }

}


