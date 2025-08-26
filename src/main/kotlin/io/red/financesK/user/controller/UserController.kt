package io.red.financesK.user.controller

import io.red.financesK.auth.controller.response.AuthUserResponse
import io.red.financesK.user.controller.request.CreateUserRequest
import io.red.financesK.user.service.create.CreateUserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
class UserController(
    private val createUserService: CreateUserService
) {

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<AuthUserResponse> {
        val user = createUserService.execute(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(user)
    }
}
