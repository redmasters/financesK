package io.red.financesK.account.balance.service.search

import io.red.financesK.account.balance.repository.AccountBalanceHistoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class SearchBalanceAccountService(
    private val accountBalanceHistoryRepository: AccountBalanceHistoryRepository
) {
    private val log = LoggerFactory.getLogger(SearchBalanceAccountService::class.java)

    fun getSumBalanceByUserAndDateRangeAndAccounts(
        userId: Int,
        startDate: String,
        endDate: String,
        accountIds: List<Int>
    ): BigDecimal {
        log.info(
            "m='getSumBalanceByUserAndDateRangeAndAccounts', action='retrieving total balance', userId='{}', startDate='{}', endDate='{}', accountIds='{}'",
            userId, startDate, endDate, accountIds
        )

        val startDateParsed = LocalDate.parse(startDate)
        val endDateParsed = LocalDate.parse(endDate)

        val totalBalance = accountBalanceHistoryRepository.getTotalBalanceByUserAndDateRangeAndAccounts(
            userId, startDateParsed, endDateParsed, accountIds
        )
        log.info(
            "m='getSumBalanceByUserAndDateRangeAndAccounts', action='total balance retrieved successfully', totalBalance='{}'",
            totalBalance
        )
        return totalBalance
    }

//    fun getSumBalanceWithLastUpdateByUserAndDateRangeAndAccounts(
//        userId: Int,
//        startDate: String,
//        endDate: String,
//        accountIds: List<Int>
//    ): List<BalanceSummaryResponse> {
//        log.info(
//            "m='getSumBalanceWithLastUpdateByUserAndDateRangeAndAccounts', action='retrieving total balance with last update', userId='{}', startDate='{}', endDate='{}', accountIds='{}'",
//            userId, startDate, endDate, accountIds
//        )
//
//        val startDateParsed = LocalDate.parse(startDate)
//        val endDateParsed = LocalDate.parse(endDate)
//
//        val balanceSummary = accountBalanceHistoryRepository.getTotalBalanceWithLastUpdateByUserAndDateRangeAndAccounts(
//            userId, startDateParsed, endDateParsed, accountIds
//        )
//
//        return balanceSummary
//    }
}
