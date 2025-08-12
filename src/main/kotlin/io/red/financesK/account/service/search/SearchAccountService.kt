package io.red.financesK.account.service.search

import io.red.financesK.account.repository.AccountRepository
import org.springframework.stereotype.Service

@Service
class SearchAccountService(
    private val accountRepository: AccountRepository
) {
    fun findAccountById(accountId: Int) =
        accountRepository.findById(accountId).orElseThrow {
            IllegalArgumentException("Account with ID $accountId not found")
        }
}
