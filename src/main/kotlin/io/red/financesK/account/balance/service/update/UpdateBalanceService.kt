package io.red.financesK.account.balance.service.update

import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.transaction.model.Transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UpdateBalanceService(
    private val operationBalanceService: OperationBalanceService
) {
    private val log = LoggerFactory.getLogger(UpdateBalanceService::class.java)

    fun executeOperation(transaction: Transaction) {
        log.info("m='updateBalance', acao='atualizando saldo após criação de transação', transaction='{}'", transaction)
        // TODO: Atualizar expense e income para fixo: income sao somados assim como expenses
        // pago ou nao o saldo eh o resultado entre income e expense

        if (transaction.status != PaymentStatus.PAID) {
            log.info("m='updateBalance', acao='transação não está paga, saldo não será atualizado'")
            return
        }

        if (transaction.type == TransactionType.EXPENSE) {
            log.info("m='updateBalance', acao='subtraindo saldo para transação de despesa'")

            operationBalanceService.subtractBalance(
                accountId = transaction.accountId?.accountId,
                amount = transaction.amount,
                operationType = AccountOperationType.fromString(transaction.operationType?.name ?: "DEBIT"),
                transaction = transaction
            )
        } else if (transaction.type == TransactionType.INCOME) {
            operationBalanceService.sumBalance(
                accountId = transaction.accountId?.accountId,
                amount = transaction.amount,
                operationType = AccountOperationType.fromString(transaction.operationType?.name ?: "CREDIT"),
                transaction = transaction

            )
        }
    }
}
