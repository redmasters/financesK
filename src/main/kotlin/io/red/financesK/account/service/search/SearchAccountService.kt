package io.red.financesK.account.service.search

import io.red.financesK.account.controller.response.AccountResponse
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.user.repository.AppUserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

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
                name = account.accountName,
                description = account.accountDescription,
                balance = account.accountInitialBalance?.toString(),
                currency = account.accountCurrency,
                userId = user.id
            )
        }
    }

    fun searchAccountById(accountId: Int): AccountResponse {
        log.info("m='searchAccountById', action='searching account by id', accountId='{}'", accountId)

        val account = accountRepository.findById(accountId).orElseThrow {
            IllegalArgumentException("Account with id $accountId not found")
        }

        return AccountResponse(
            accountId = account.accountId,
            name = account.accountName,
            description = account.accountDescription,
            balance = account.accountInitialBalance?.toString(),
            currency = account.accountCurrency,
            userId = account.userId?.id
        )
    }
}
