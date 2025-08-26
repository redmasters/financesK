package io.red.financesK.auth.service

import io.red.financesK.auth.controller.request.LoginRequest
import io.red.financesK.auth.controller.response.AuthUserResponse
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.user.service.search.SearchUserService
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val searchUserService: SearchUserService
) {
    fun login(request: LoginRequest): AuthUserResponse {
        val user = searchUserService.searchUserByUsernameOrEmail(request.username)

        if (user?.passwordHash == request.password) {
            return AuthUserResponse(
                id = user.id ?: 0,
                username = user.username ?: "",
                email = user.email ?: "",
                token = "dummy-token" // In a real application, generate a JWT or similar token here
            )
        } else {
            throw ValidationException("Invalid username or password")
        }


    }
}
