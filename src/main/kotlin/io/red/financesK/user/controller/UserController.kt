package io.red.financesK.user.controller

import io.red.financesK.auth.controller.response.AuthUserResponse
import io.red.financesK.user.controller.request.CreateUserRequest
import io.red.financesK.user.controller.request.UpdateUserRequest
import io.red.financesK.user.controller.response.GenericResponse
import io.red.financesK.user.service.create.CreateUserService
import io.red.financesK.user.service.update.UpdateUserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

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
        return ResponseEntity.status(HttpStatus.CREATED)
            .body("If the email is registered, a password reset link will be sent.")
    }

    @PostMapping("/change-password")
    fun showChangePasswordPage(
        locale: Locale,
        @RequestParam token: String
    ): ResponseEntity<Boolean> {

        val result = updateUserService.changePassword(token)
        if (result) {
            return ResponseEntity.status(HttpStatus.OK).body(true)
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false)
    }

    @PostMapping("/save-password")
    fun savePassword(
        locale: Locale,
        @RequestParam token: String,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<GenericResponse> {

        val passRequest = UpdateUserRequest(
            username = "",
            email = "",
            oldPassword = "",
            newPassword = request.newPassword,
            confirmPassword = request.confirmPassword,
            pathAvatar = "",
            token = token
        )


        val result = updateUserService.savePassword(locale, passRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

}

