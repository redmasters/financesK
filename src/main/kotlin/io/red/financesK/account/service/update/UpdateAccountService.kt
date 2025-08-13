package io.red.financesK.account.service.update

import io.red.financesK.account.controller.request.UpdateAccountRequest
import io.red.financesK.account.controller.response.UpdateAccountResponse
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.global.exception.NotFoundException
import io.red.financesK.global.utils.ConvertMoneyUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UpdateAccountService(
    private val accountRepository: AccountRepository
) {
    private val log = LoggerFactory.getLogger(UpdateAccountService::class.java)

    fun execute(accountId: Int, request: UpdateAccountRequest): UpdateAccountResponse {
        log.info("m='execute', acao='atualizando conta', accountId='$accountId', request='$request'")

        val account = accountRepository.findById(accountId)
            .orElseThrow {
                log.error("m='execute', acao='conta não encontrada', accountId='$accountId'")
                NotFoundException("Account not found with id: $accountId")
            }

        // Update only non-null fields
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

        account.updatedAt = Instant.now()

        val updatedAccount = accountRepository.save(account)

        log.info("m='execute', acao='conta atualizada com sucesso', accountId='$accountId'")

        return UpdateAccountResponse(
            accountId = updatedAccount.accountId!!,
            updatedAt = updatedAccount.updatedAt!!
        )
    }
}
