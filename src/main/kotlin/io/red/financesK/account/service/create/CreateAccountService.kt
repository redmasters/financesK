package io.red.financesK.account.service.create

import io.red.financesK.account.controller.request.CreateAccountRequest
import io.red.financesK.account.controller.response.CreateAccountResponse
import io.red.financesK.account.enums.AccountTypeEnum
import io.red.financesK.account.model.Account
import io.red.financesK.account.repository.AccountRepository
import io.red.financesK.bank.service.search.SearchBankService
import io.red.financesK.global.utils.ConvertMoneyUtils
import io.red.financesK.user.service.search.SearchUserService
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class CreateAccountService(
    private val accountRepository: AccountRepository,
    private val searchUserService: SearchUserService,
    private val searchBankService: SearchBankService
) {
    fun execute(request: CreateAccountRequest): CreateAccountResponse {
        val user = searchUserService.findUserById(request.userId)
        val bankInstitution = request.bankInstitutionId?.let { searchBankService.findBankById(it) }
        val account = Account(
            accountName = request.accountName,
            accountDescription = request.accountDescription,
            accountCurrentBalance = ConvertMoneyUtils.convertToCents(request.accountCurrentBalance ?: BigDecimal.ZERO),
            accountCurrency = request.accountCurrency,
            bankInstitution = bankInstitution,
            accountType = AccountTypeEnum.fromString(request.accountType),
            accountCreditLimit = request.accountCreditLimit?.let { ConvertMoneyUtils.convertToCents(it) },
            accountStatementClosingDate = request.accountStatementClosingDate,
            accountPaymentDueDate = request.accountPaymentDueDate,
            userId = user
        )
        val savedAccount = accountRepository.save(account)
        return CreateAccountResponse(
            accountId = savedAccount.accountId,
            message = "Account created successfully",
            createdAt = savedAccount.createdAt
        )
    }
}
