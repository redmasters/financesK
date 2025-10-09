package io.red.financesK.transaction.event

import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.global.exception.NotFoundException
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TransactionEventHandler(
    private val accountRepository: AccountRepository
) {
    private val log = LoggerFactory.getLogger(TransactionEventHandler::class.java)

    @EventListener
    fun handleTransactionCreatedEvent(event: TransactionCreatedEvent) {
        log.info("m='handleTransactionCreatedEvent', acao='processando evento de criacao', transactionId='${event.transactionId}', accountId='${event.accountId}'")
        if (event.status == PaymentStatus.PENDING) return
        updateAccountBalance(event.accountId, event.amount, event.type)
    }

    @EventListener
    fun handleTransactionStatusChangedEvent(event: TransactionStatusChangedEvent) {
        log.info("m='handleTransactionStatusChangedEvent', acao='processando mudanca de status', transactionId='${event.transactionId}', previousStatus='${event.previousStatus}', newStatus='${event.newStatus}'")

        if (event.newStatus == PaymentStatus.PAID && event.previousStatus != PaymentStatus.PAID) {
            // Status mudou para PAID - aplicar valor
            updateAccountBalance(event.accountId, event.amount, event.type)
        } else if (event.previousStatus == PaymentStatus.PAID && event.newStatus != PaymentStatus.PAID) {
            // Status saiu de PAID - reverter valor
            updateAccountBalance(event.accountId, -event.amount, event.type)
        }
    }

    private fun updateAccountBalance(accountId: Int?, amount: Int, type: TransactionType?) {
        log.info(
            "m='updateAccountBalance', acao='atualizando saldo da conta'," +
                    " accountId='$accountId', amount='$amount', type='$type'"
        )

        val account = accountRepository.findById(accountId!!).orElseThrow {
            NotFoundException("Account with ID $accountId not found")
        }

        val balanceChange = if (type == TransactionType.EXPENSE) -amount else amount
        val currentBalance = account.accountCurrentBalance ?: 0
        val newBalance = currentBalance + balanceChange

        account.updatedAt = Instant.now()
        account.accountCurrentBalance = newBalance
        accountRepository.save(account)

        log.info(
            "m='updateAccountBalance', acao='saldo atualizado com sucesso', accountId='$accountId'," +
                    " balanceAnterior='$currentBalance', novoSaldo='$newBalance', mudanca='$balanceChange'"
        )
    }
}
