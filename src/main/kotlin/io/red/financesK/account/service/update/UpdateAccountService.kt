package io.red.financesK.account.service.update

import io.red.financesK.account.controller.request.UpdateAccountRequest
import io.red.financesK.account.controller.response.UpdateAccountResponse
import io.red.financesK.account.enums.AccountTypeEnum
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.bank.service.search.SearchBankService
import io.red.financesK.global.exception.NotFoundException
import io.red.financesK.global.utils.ConvertMoneyUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UpdateAccountService(
    private val accountRepository: AccountRepository,
    private val searchBankService: SearchBankService
) {
    private val log = LoggerFactory.getLogger(UpdateAccountService::class.java)

    fun execute(accountId: Int, request: UpdateAccountRequest): UpdateAccountResponse {
        log.info("m='execute', acao='atualizando conta', accountId='$accountId', request='$request'")

        val account = accountRepository.findById(accountId)
            .orElseThrow {
                log.error("m='execute', acao='conta não encontrada', accountId='$accountId'")
                NotFoundException("Account not found with id: $accountId")
            }

        request.accountName?.let {
            log.debug("m='execute', acao='atualizando nome da conta', accountId='$accountId', newName='$it'")
            account.accountName = it
        }

        request.accountDescription?.let {
            log.debug("m='execute', acao='atualizando descrição da conta', accountId='$accountId'")
            account.accountDescription = it
        }

        request.accountCurrentBalance?.let { balance ->
            log.debug("m='execute', acao='atualizando saldo da conta', accountId='$accountId', newBalance='$balance'")
            account.accountCurrentBalance = ConvertMoneyUtils.convertToCents(balance)
        }

        request.accountCurrency?.let {
            log.debug("m='execute', acao='atualizando moeda da conta', accountId='$accountId', newCurrency='$it'")
            account.accountCurrency = it
        }

        request.bankInstitutionId?.let { bankId ->
            log.debug("m='execute', acao='atualizando instituição bancária da conta', accountId='$accountId', newBankId='$bankId'")
            val bank = searchBankService.findBankById(bankId)
            account.bankInstitution = bank
        }
        request.accountType?.let {
            log.debug("m='execute', acao='atualizando tipo da conta', accountId='$accountId', newAccountType='$it'")
            account.accountType = AccountTypeEnum.fromString(it)
        }
        request.accountCreditLimit?.let { creditLimit ->
            log.debug("m='execute', acao='atualizando limite de crédito da conta', accountId='$accountId', newCreditLimit='$creditLimit'")
            account.accountCreditLimit = ConvertMoneyUtils.convertToCents(creditLimit)
        }
        request.accountStatementClosingDate?.let {
            log.debug("m='execute', acao='atualizando data de fechamento da conta', accountId='$accountId', newStatementClosingDate='$it'")
            account.accountStatementClosingDate = it
        }

        request.accountPaymentDueDate?.let {
            log.debug("m='execute', acao='atualizando data de vencimento da conta', accountId='$accountId', newPaymentDueDate='$it'")
            account.accountPaymentDueDate = it
        }

        account.updatedAt = Instant.now()

        val updatedAccount = accountRepository.save(account)

        log.info("m='execute', acao='conta atualizada com sucesso', accountId='$accountId'")

        return UpdateAccountResponse(
            accountId = updatedAccount.accountId!!,
            updatedAt = updatedAccount.updatedAt!!
        )
    }
}
