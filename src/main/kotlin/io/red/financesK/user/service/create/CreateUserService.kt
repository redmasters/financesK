package io.red.financesK.user.service.create

import io.red.financesK.auth.controller.response.AuthUserResponse
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.user.controller.request.CreateUserRequest
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CreateUserService(
    private val userRepository: AppUserRepository,

) {
    private val log = LoggerFactory.getLogger(CreateUserService::class.java)

    fun execute(request: CreateUserRequest): AuthUserResponse {

        isUsernameTaken(request.username)

        log.info("Creating user with username: ${request.username}")
        val user = AppUser(
            username = request.username,
            email = request.email,
            passwordHash = request.password, // In a real application, you should hash the password
            pathAvatar = request.pathAvatar ?: "default-avatar.png", // Default avatar if none provided
            createdAt = Instant.now()
        )
        val savedUser = userRepository.save(user)
        log.info("User created successfully")
        return AuthUserResponse(
            id = savedUser.id ?: 0,
            username = savedUser.username ?: "",
            email = savedUser.email ?: "",
            token = "dummy-token" // In a real application, generate a JWT or similar token here
        )
    }

    private fun isUsernameTaken(username: String) {
        log.info("Checking if username $username is already taken")
        if (userRepository.existsByUsername(username)) {
            log.error("Username $username is already taken")
            throw ValidationException("Username $username is already taken")
        }
        log.info("Username $username is available")
    }
}
