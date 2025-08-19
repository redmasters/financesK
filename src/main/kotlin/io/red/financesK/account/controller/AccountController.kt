package io.red.financesK.account.controller

import io.red.financesK.account.controller.request.CreateAccountRequest
import io.red.financesK.account.controller.request.UpdateAccountRequest
import io.red.financesK.account.controller.response.AccountResponse
import io.red.financesK.account.controller.response.CreateAccountResponse
import io.red.financesK.account.controller.response.DeleteAccountResponse
import io.red.financesK.account.controller.response.UpdateAccountResponse
import io.red.financesK.account.service.create.CreateAccountService
import io.red.financesK.account.service.delete.DeleteAccountService
import io.red.financesK.account.service.search.SearchAccountService
import io.red.financesK.account.service.update.UpdateAccountService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/accounts")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class AccountController(
    private val createAccountService: CreateAccountService,
    private val updateAccountService: UpdateAccountService,
    private val searchAccountService: SearchAccountService,
    private val deleteAccountService: DeleteAccountService
) {
    @PostMapping
    fun createAccount(@RequestBody @Valid request: CreateAccountRequest): ResponseEntity<CreateAccountResponse> {
        val account = createAccountService.execute(request)
        return ResponseEntity.status(201).body(
            CreateAccountResponse(
                accountId = account.accountId,
                message = "Account created successfully",
                createdAt = account.createdAt
            )
        )
    }

    @GetMapping("/{accountId}")
    fun getAccountById(@PathVariable accountId: Int): ResponseEntity<AccountResponse> {
        val response = searchAccountService.getAccountById(accountId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/user/{userId}")
    fun getAccountsByUserId(@PathVariable userId: Int): ResponseEntity<List<AccountResponse>> {
        val accounts = searchAccountService.getListAccountsByUserId(userId)
        return if (accounts.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(accounts)
        }
    }

    @PutMapping("/{accountId}")
    fun updateAccount(
        @PathVariable accountId: Int,
        @RequestBody @Valid request: UpdateAccountRequest
    ): ResponseEntity<UpdateAccountResponse> {
        val response = updateAccountService.execute(accountId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{accountId}")
    fun deleteAccount(@PathVariable accountId: Int): ResponseEntity<DeleteAccountResponse> {
        val response = deleteAccountService.execute(accountId)
        return ResponseEntity.ok(response)
    }
}
