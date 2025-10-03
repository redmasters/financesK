package io.red.financesK.user.controller

import io.red.financesK.auth.controller.response.AuthUserResponse
import io.red.financesK.user.controller.request.CreateUserRequest
import io.red.financesK.user.controller.request.UpdateUserRequest
import io.red.financesK.user.service.create.CreateUserService
import io.red.financesK.user.service.update.UpdateUserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val createUserService: CreateUserService,
    private val updateUserService: UpdateUserService
) {

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<AuthUserResponse> {
        val user = createUserService.execute(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(user)
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Int,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<String> {
        updateUserService.updateUser(id, request)
        return ResponseEntity.status(HttpStatus.OK).body("Success")
    }


    @PostMapping("/reset-password")
    fun resetPassword(
        request: HttpServletRequest,
        @RequestParam email: String
    ): ResponseEntity<String> {
        updateUserService.resetPassword(request, email)
        return ResponseEntity.status(HttpStatus.OK)
            .body("If the email is registered, a password reset link will be sent.")
    }

}
