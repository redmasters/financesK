package io.red.financesK.account.service.create

import io.red.financesK.account.controller.request.CreateAccountRequest
import io.red.financesK.account.model.Account
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.RoundingMode

@Service
class CreateAccountService(
    private val accountRepository: io.red.financesK.account.repository.AccountRepository,
    private val appUserRepository: io.red.financesK.user.repository.AppUserRepository
) {
    private val log = LoggerFactory.getLogger(CreateAccountService::class.java)

    fun createAccount(request: CreateAccountRequest) {
        log.info("m='createAccount', action='creating account', request='{}'", request)

        val user = appUserRepository.findById(request.userId).orElseThrow {
            IllegalArgumentException("User with id ${request.userId} not found")
        }

        val account = Account(
            accountName = request.name,
            accountDescription = request.description,
            accountInitialBalance = request.balance
                ?.toBigDecimalOrNull()?.setScale(2, RoundingMode.HALF_EVEN),
            accountCurrency = request.currency,
            userId = user
        )

        accountRepository.save(account)
        log.info("m='createAccount', action='account created successfully', account='{}'", account)
    }
}
