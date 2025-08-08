package io.red.financesK.account.service.search

import io.red.financesK.account.balance.controller.response.BalanceAccountResponse
import io.red.financesK.account.balance.controller.response.BalanceSummaryResponse
import io.red.financesK.account.controller.response.AccountResponse
import io.red.financesK.account.model.Account
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.user.repository.AppUserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SearchAccountService(
    private val accountRepository: AccountRepository,
    private val appUserRepository: AppUserRepository
) {
    private val log = LoggerFactory.getLogger(SearchAccountService::class.java)

    fun searchAccountsByUserId(userId: Int): List<AccountResponse> {
        log.info("m='searchAccountsByUserId', action='searching accounts for user', userId='{}'", userId)

        val user = appUserRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User with id $userId not found")
        }

        val accounts = accountRepository.findAllByUserId(user)

        log.info("m='searchAccountsByUserId', action='accounts found', count='{}'", accounts)
        return accounts.map { account ->
            AccountResponse(
                accountId = account.accountId,
                name = account.accountName ?: "",
                description = account.accountDescription,
                balance = account.accountInitialBalance?.toString(),
                currency = account.accountCurrency ?: "BRL",
                userId = user.id
            )
        }
    }

    fun searchAccountById(accountId: Int?): Account {
        log.info("m='searchAccountById', action='searching account by id', accountId='{}'", accountId)

        require(accountId != null) { "Account ID cannot be null" }
        val account = accountRepository.findById(accountId).orElseThrow {
            IllegalArgumentException("Account with id $accountId not found")
        }

        return account
    }

    fun getBalanceByAccountList(accountIds: List<Int>): List<BalanceAccountResponse> {
        log.info("m='getBalanceByAccountList', action='getting balance for account list', accountIds='{}'", accountIds)

        val accounts = accountRepository.findAllById(accountIds)
        if (accounts.isEmpty()) {
            log.warn("m='getBalanceByAccountList', action='no accounts found for provided ids'")
            return emptyList()
        }

        return accounts.map { account ->
            BalanceAccountResponse(
                accountId = account.accountId,
                balance = account.accountInitialBalance?.toString() ?: "0.00",
                currency = account.accountCurrency,
                userId = account.userId?.id ?: 0,
                updatedAt = account.updatedAt?.toString() ?: "N/A"
            )
        }

    }

    fun getBalanceBy(
        userId: Int,
        startDate: String,
        endDate: String,
        accountIds: List<Int>
    ): BalanceSummaryResponse {
        log.info(
            "m='getBalanceBy', action='getting balance summary by user and date range and accounts', userId='{}', startDate='{}', endDate='{}', accountIds='{}'",
            userId, startDate, endDate, accountIds
        )

        return accountRepository.getTotalBalanceWithLastUpdateByUserAndDateRangeAndAccounts(
            userId, LocalDate.parse(startDate), LocalDate.parse(endDate), accountIds
        )

    }
}
