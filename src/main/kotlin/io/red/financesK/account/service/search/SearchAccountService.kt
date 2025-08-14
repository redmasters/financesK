package io.red.financesK.account.service.search

import io.red.financesK.account.controller.response.AccountResponse
import io.red.financesK.account.model.Account
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

    fun findAccountById(accountId: Int): Account? =
        accountRepository.findById(accountId).orElseThrow {
            IllegalArgumentException("Account with ID $accountId not found")
        }

    fun getListAccountsByUserId(userId: Int): List<AccountResponse> {
        log.info("m='getListAccountsByUserId', acao='buscando contas por userId', userId='$userId'")

        val accounts = accountRepository.findAllByUserId(userId)

        if (accounts.isEmpty()) {
            log.warn("m='getListAccountsByUserId', acao='nenhuma conta encontrada para o userId', userId='$userId'")
            return emptyList()
        }

        log.info("m='getListAccountsByUserId', acao='contas encontradas com sucesso', userId='$userId'")

        return accounts.map { account ->
            toAccountResponse(account)
        }
    }

    fun getAccountById(accountId: Int): AccountResponse {
        log.info("m='getAccountById', acao='buscando conta por id', accountId='$accountId'")

        val account = accountRepository.findById(accountId)
            .orElseThrow {
                log.error("m='getAccountById', acao='conta não encontrada', accountId='$accountId'")
                NotFoundException("Account not found with id: $accountId")
            }

        log.info("m='getAccountById', acao='conta encontrada com sucesso', accountId='$accountId'")

        return toAccountResponse(account)
    }

    fun toAccountResponse(account: Account): AccountResponse = AccountResponse(
        accountId = account.accountId!!,
        accountName = account.accountName ?: "",
        accountDescription = account.accountDescription,
        accountType = account.accountType?.name ?: "",
        bankInstitutionName = account.bankInstitution?.institutionName ?: "",
        accountCreditLimit = ConvertMoneyUtils.convertToDecimal(account.accountCreditLimit ?: 0),
        accountStatementClosingDate = account.accountStatementClosingDate,
        accountPaymentDueDate = account.accountPaymentDueDate,
        accountCurrentBalance = ConvertMoneyUtils.convertToDecimal(account.accountCurrentBalance ?: 0),
        accountCurrency = account.accountCurrency ?: "BRL",
        userId = account.userId?.id ?: 0,
        userName = account.userId?.username,
        createdAt = account.createdAt,
        updatedAt = account.updatedAt
    )
}
