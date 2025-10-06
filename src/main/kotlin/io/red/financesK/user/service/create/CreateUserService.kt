package io.red.financesK.user.service.create

import io.red.financesK.auth.controller.response.AuthUserResponse
import io.red.financesK.auth.model.Authority
import io.red.financesK.auth.service.AuthService
import io.red.financesK.auth.service.PasswordService
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.user.controller.request.CreateUserRequest
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.Locale.getDefault

@Service
@Transactional
class CreateUserService(
    private val userRepository: AppUserRepository,
    private val authService: AuthService,
    private val passwordService: PasswordService

) {
    private val log = LoggerFactory.getLogger(CreateUserService::class.java)

    fun execute(request: CreateUserRequest): AuthUserResponse {

        isUsernameOrEmailTaken(request.username, request.email)

        log.info("Creating user with username: ${request.username}")
        val user = AppUser(
            username = request.username.lowercase(getDefault()),
            email = request.email,
            passwordHash = hashPassword(request.password),
            passwordSalt = saltPassword(),
            pathAvatar = request.pathAvatar ?: "default-avatar.png", // Default avatar if none provided
            createdAt = Instant.now(),
            authorities = mutableSetOf(Authority.USER) // Define USER como authority padrão
        )
        val savedUser = userRepository.save(user)
        log.info("User created successfully with USER authority")
        return AuthUserResponse(
            id = savedUser.id ?: 0,
            username = savedUser.username ?: "",
            email = savedUser.email ?: "",
            token = authService.getTokenFromUserId(savedUser.id!!.toLong())
        )
    }

    private fun isUsernameOrEmailTaken(username: String, email: String) {
        log.info("Checking if username $username is already taken")
        if (userRepository.existsByUsernameOrEmail(username, email)) {
            log.error("Username $username is already taken")
            throw ValidationException("Username $username is already taken")
        }
        log.info("Username $username is available")
    }

    private fun hashPassword(password: String): String {
        return passwordService.encode(password)
    }

    private fun saltPassword(): String {
        return passwordService.saltPassword()
    }


}
