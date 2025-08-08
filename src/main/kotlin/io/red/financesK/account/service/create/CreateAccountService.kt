package io.red.financesK.account.service.create

import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.account.balance.service.history.CreateBalanceHistory
import io.red.financesK.account.controller.request.CreateAccountRequest
import io.red.financesK.account.model.Account
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class CreateAccountService(
    private val accountRepository: AccountRepository,
    private val appUserRepository: AppUserRepository,
    private val createBalanceHistory: CreateBalanceHistory
) {

    companion object {
        private val log = LoggerFactory.getLogger(CreateAccountService::class.java)
        private const val DEFAULT_SCALE = 2
        private val DEFAULT_ROUNDING = RoundingMode.HALF_EVEN
    }

    @Transactional
    fun createAccount(request: CreateAccountRequest): CreateAccountResponse {
        log.info("m='createAccount', action='starting account creation', userId='{}'", request.userId)

        validateRequest(request)
        val user = findUserById(request.userId)
        val initialBalance = parseBalance(request.balance)

        val account = buildAccount(request, user, initialBalance)
        val savedAccount = saveAccount(account)

        createInitialBalanceHistory(savedAccount, initialBalance)

        log.info("m='createAccount', action='account created successfully', accountId='{}'", savedAccount.accountId)

        return CreateAccountResponse(
            accountId = savedAccount.accountId,
            name = savedAccount.accountName ?: "",
            description = savedAccount.accountDescription,
            balance = savedAccount.accountInitialBalance?.toString() ?: "0.00",
            currency = savedAccount.accountCurrency ?: "BRL",
            userId = savedAccount.userId?.id ?: request.userId
        )
    }

    private fun validateRequest(request: CreateAccountRequest) {
        log.debug("m='validateRequest', action='validating create account request'")

        if (request.name.isBlank()) {
            throw ValidationException("Account name cannot be blank")
        }

        if (request.name.length > 100) {
            throw ValidationException("Account name cannot exceed 100 characters")
        }

        if (request.description?.length ?: 0 > 500) {
            throw ValidationException("Account description cannot exceed 500 characters")
        }

        if (request.currency.isBlank()) {
            throw ValidationException("Account currency cannot be blank")
        }

        if (request.currency.length != 3) {
            throw ValidationException("Currency must be a 3-letter code (e.g., BRL, USD)")
        }
    }

    private fun findUserById(userId: Int): AppUser {
        log.debug("m='findUserById', action='searching user', userId='{}'", userId)

        return appUserRepository.findById(userId).orElseThrow {
            ValidationException("User with id $userId not found")
        }
    }

    private fun parseBalance(balanceStr: String?): BigDecimal {
        log.debug("m='parseBalance', action='parsing balance', balance='{}'", balanceStr)

        if (balanceStr.isNullOrBlank()) {
            return BigDecimal.ZERO
        }

        return try {
            val balance = balanceStr.toBigDecimal().setScale(DEFAULT_SCALE, DEFAULT_ROUNDING)

            if (balance < BigDecimal.ZERO) {
                throw ValidationException("Initial balance cannot be negative")
            }

            balance
        } catch (e: NumberFormatException) {
            log.warn("m='parseBalance', action='invalid balance format', balance='{}'", balanceStr)
            BigDecimal.ZERO
        }
    }

    private fun buildAccount(
        request: CreateAccountRequest,
        user: AppUser,
        initialBalance: BigDecimal
    ): Account {
        log.debug("m='buildAccount', action='building account entity'")

        return Account(
            accountName = request.name.trim(),
            accountDescription = request.description?.trim(),
            accountInitialBalance = initialBalance,
            accountCurrency = request.currency.uppercase(),
            userId = user
        )
    }

    private fun saveAccount(account: Account): Account {
        log.debug("m='saveAccount', action='persisting account'")

        return try {
            accountRepository.save(account)
        } catch (e: Exception) {
            log.error("m='saveAccount', action='error saving account', error='{}'", e.message)
            throw ValidationException("Failed to create account: ${e.message}")
        }
    }

    private fun createInitialBalanceHistory(account: Account, initialBalance: BigDecimal) {
        log.debug("m='createInitialBalanceHistory', action='creating balance history', accountId='{}'", account.accountId)

        if (account.accountId == null) {
            log.warn("m='createInitialBalanceHistory', action='account id is null, skipping balance history'")
            return
        }

        try {
            createBalanceHistory.createBalanceHistory(
                accountId = account.accountId,
                amount = initialBalance,
                operationType = AccountOperationType.INITIAL_BALANCE,
                transactionId = null
            )
        } catch (e: Exception) {
            log.error("m='createInitialBalanceHistory', action='error creating balance history', error='{}'", e.message)
        }
    }
}

data class CreateAccountResponse(
    val accountId: Int?,
    val name: String,
    val description: String?,
    val balance: String,
    val currency: String,
    val userId: Int
)
