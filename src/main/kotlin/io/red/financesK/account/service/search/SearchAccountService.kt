package io.red.financesK.account.service.search

import io.red.financesK.account.controller.response.AccountResponse
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.global.exception.NotFoundException
import io.red.financesK.global.utils.ConvertMoneyUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SearchAccountService(
    private val accountRepository: AccountRepository
) {
    private val log = LoggerFactory.getLogger(SearchAccountService::class.java)

    fun findAccountById(accountId: Int) =
        accountRepository.findById(accountId).orElseThrow {
            IllegalArgumentException("Account with ID $accountId not found")
        }

    fun getAccountById(accountId: Int): AccountResponse {
        log.info("m='getAccountById', acao='buscando conta por id', accountId='$accountId'")

        val account = accountRepository.findById(accountId)
            .orElseThrow {
                log.error("m='getAccountById', acao='conta não encontrada', accountId='$accountId'")
                NotFoundException("Account not found with id: $accountId")
            }

        log.info("m='getAccountById', acao='conta encontrada com sucesso', accountId='$accountId'")

        return AccountResponse(
            accountId = account.accountId!!,
            accountName = account.accountName ?: "",
            accountDescription = account.accountDescription,
            accountCurrentBalance = ConvertMoneyUtils.convertToDecimal(account.accountCurrentBalance ?: 0),
            accountCurrency = account.accountCurrency ?: "BRL",
            userId = account.userId?.id ?: 0,
            userName = account.userId?.username,
            createdAt = account.createdAt,
            updatedAt = account.updatedAt
        )
    }
}
