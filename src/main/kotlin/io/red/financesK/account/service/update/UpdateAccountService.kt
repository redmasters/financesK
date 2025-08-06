package io.red.financesK.account.service.update

import io.red.financesK.account.controller.request.UpdateAccountRequest
import io.red.financesK.account.controller.response.UpdateAccountResponse
import org.springframework.stereotype.Service
import java.math.RoundingMode

@Service
class UpdateAccountService(
    private val accountRepository: io.red.financesK.account.repository.AccountRepository,
    private val appUserRepository: io.red.financesK.user.repository.AppUserRepository
) {
    private val log = org.slf4j.LoggerFactory.getLogger(UpdateAccountService::class.java)

    fun updateAccount(accountId: Int, request: UpdateAccountRequest): UpdateAccountResponse {
        log.info("m='updateAccount', action='updating account', accountId='{}', request='{}'", accountId, request)

        val account = accountRepository.findById(accountId).orElseThrow {
            IllegalArgumentException("Account with id $accountId not found")
        }
        val user = appUserRepository.findById(request.userId).orElseThrow {
            IllegalArgumentException("User with id ${request.userId} not found")
        }
        account.accountName = request.name
        account.accountDescription = request.description
        account.accountInitialBalance = request.balance
            ?.toBigDecimalOrNull()?.setScale(2, RoundingMode.HALF_EVEN)
        account.accountCurrency = request.currency
        account.userId = user
        val updatedAccount = accountRepository.save(account)
        log.info("m='updateAccount', action='account updated successfully', account='{}'", account)
        return UpdateAccountResponse(
            accountId = updatedAccount.accountId,
            name = account.accountName,
            description = account.accountDescription,
            balance = account.accountInitialBalance?.toString(),
            currency = account.accountCurrency,
            userId = user.id
        )
    }

    fun updateAccountBalance(accountId: Int, newBalance: String): UpdateAccountResponse {
        log.info(
            "m='updateAccountBalance', action='updating account balance', accountId='{}', newBalance='{}'",
            accountId,
            newBalance
        )

        val account = accountRepository.findById(accountId).orElseThrow {
            IllegalArgumentException("Account with id $accountId not found")
        }
        account.accountInitialBalance = newBalance.toBigDecimalOrNull()?.setScale(2, RoundingMode.HALF_EVEN)
            ?: throw IllegalArgumentException("Invalid balance format: $newBalance")
        val updatedAccount = accountRepository.save(account)
        log.info(
            "m='updateAccountBalance', action='account balance updated successfully', account='{}'",
            updatedAccount
        )
        return UpdateAccountResponse(
            accountId = updatedAccount.accountId,
            name = updatedAccount.accountName,
            description = updatedAccount.accountDescription,
            balance = updatedAccount.accountInitialBalance?.toString(),
            currency = updatedAccount.accountCurrency,
            userId = updatedAccount.userId?.id ?: throw IllegalArgumentException("User ID not found")
        )
    }
}
