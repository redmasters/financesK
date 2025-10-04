package io.red.financesK.auth.service

import io.red.financesK.auth.controller.request.LoginRequest
import io.red.financesK.auth.controller.response.AuthUserResponse
import io.red.financesK.auth.jwt.JwtTokenProvider
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.user.service.search.SearchUserService
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val searchUserService: SearchUserService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsServiceImpl: UserDetailsServiceImpl,
    private val passwordService: PasswordService
) {
    fun login(request: LoginRequest): AuthUserResponse {
        val user = searchUserService.searchUserByUsernameOrEmail(request.username, request.email)
        val encodedPass = user?.passwordHash ?: throw ValidationException("Invalid username or password")

        if (validatePassword(request.password, encodedPass)) {
            return AuthUserResponse(
                id = user.id ?: 0,
                username = user.username ?: "",
                email = user.email ?: "",
                token = getTokenFromUserId(user.id!!.toLong())
            )
        } else {
            throw ValidationException("Invalid username or password")
        }

    }

    fun getTokenFromUserId(userId: Long): String {
        val user = searchUserService.findUserById(userId.toInt())
        val customUser = userDetailsServiceImpl.loadUserByUsername(user.username!!)
        return jwtTokenProvider.generateToken(customUser as CustomUserDetails)

    }

    fun validatePassword(rawPassword: String, encodedPassword: String): Boolean {
        return passwordService.matches(rawPassword, encodedPassword)
    }

}
