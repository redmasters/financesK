package io.red.financesK.auth.controller

import io.red.financesK.auth.controller.request.LoginRequest
import io.red.financesK.auth.service.AuthService
import io.red.financesK.global.exception.ValidationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<out Any?> {
        if (request.username.isBlank() || request.password.isBlank()) {
            return ResponseEntity.badRequest().body("Username/email and password must not be empty")
        }
        return try {
            val token = authService.login(request)
            ResponseEntity.ok(token)
        } catch (e: ValidationException) {
            ResponseEntity.badRequest().body(e.message ?: "Invalid credentials")
        }
    }

}
