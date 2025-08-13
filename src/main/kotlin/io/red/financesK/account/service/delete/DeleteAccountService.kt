package io.red.financesK.account.service.delete

import io.red.financesK.account.controller.response.DeleteAccountResponse
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.global.exception.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DeleteAccountService(
    private val accountRepository: AccountRepository
) {
    private val log = LoggerFactory.getLogger(DeleteAccountService::class.java)

    fun execute(accountId: Int): DeleteAccountResponse {
        log.info("m='execute', acao='deletando conta', accountId='$accountId'")

        val account = accountRepository.findById(accountId)
            .orElseThrow {
                log.error("m='execute', acao='conta não encontrada para deletar', accountId='$accountId'")
                NotFoundException("Account not found with id: $accountId")
            }

        accountRepository.delete(account)

        log.info("m='execute', acao='conta deletada com sucesso', accountId='$accountId'")

        return DeleteAccountResponse(
            accountId = accountId
        )
    }
}
