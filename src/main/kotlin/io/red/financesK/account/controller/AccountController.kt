package io.red.financesK.account.controller

import io.red.financesK.account.controller.request.CreateAccountRequest
import io.red.financesK.account.controller.request.UpdateAccountRequest
import io.red.financesK.account.controller.response.AccountResponse
import io.red.financesK.account.service.create.CreateAccountService
import io.red.financesK.account.service.search.SearchAccountService
import io.red.financesK.account.service.update.UpdateAccountService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/accounts")
class AccountController(
    private val createAccountService: CreateAccountService,
    private val updateAccountService: UpdateAccountService,
    private val searchAccountService: SearchAccountService
) {
    @PostMapping
    fun createAccount(@RequestBody request: CreateAccountRequest): ResponseEntity<Void> {
        createAccountService.createAccount(request)
        return ResponseEntity.status(201).build()
    }

    @PutMapping("/{accountId}")
    fun updateAccount(
        @PathVariable accountId: Int,
        @RequestBody request: UpdateAccountRequest
    ): ResponseEntity<Void> {
        updateAccountService.updateAccount(accountId, request)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/user/{userId}")
    fun searchAccountsByUserId(@PathVariable userId: Int): ResponseEntity<List<AccountResponse>> {
        val accounts = searchAccountService.searchAccountsByUserId(userId)
        return if (accounts.isNotEmpty()) {
            ResponseEntity.ok(accounts)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
