package io.red.financesK.account.controller

import io.red.financesK.account.controller.request.CreateAccountRequest
import io.red.financesK.account.controller.response.CreateAccountResponse
import io.red.financesK.account.service.create.CreateAccountService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/accounts")
class AccountController(
    private val createAccountService: CreateAccountService
) {
    @PostMapping
    fun createAccount(@RequestBody request: CreateAccountRequest): ResponseEntity<CreateAccountResponse> {
        val account = createAccountService.execute(request)
        return ResponseEntity.status(201).body(
            CreateAccountResponse(
                accountId = account.accountId,
                message = "Account created successfully",
                createdAt = account.createdAt
            )
        )
    }
}
