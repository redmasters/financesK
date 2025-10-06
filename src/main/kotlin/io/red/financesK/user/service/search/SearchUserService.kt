package io.red.financesK.user.service.search

import io.red.financesK.global.exception.NotFoundException
import io.red.financesK.user.controller.response.UserResponse
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SearchUserService(
    private val userRepository: AppUserRepository
) {
    private val log = LoggerFactory.getLogger(SearchUserService::class.java)
    fun findUserById(userId: Int): AppUser {
        return userRepository.findById(userId)
            .orElseThrow {
                IllegalArgumentException("User with ID $userId not found")
            }
    }

    fun searchUserById(userId: Int): UserResponse? {
        val user = userRepository.findById(userId)
            .orElseThrow {
                NotFoundException("User with ID $userId not found")
            }

        return user?.let {
            UserResponse(
                id = it.id,
                username = it.username,
                email = it.email
            )
        }
    }

    fun searchUserByUsername(usernameEmail: String): AppUser? {
        log.info("Searching for user with username: $usernameEmail")
        val user = userRepository.findByUsernameOrderByEmail(usernameEmail)
            ?: throw NotFoundException("User with username $usernameEmail not found")
        return user
    }

    fun searchUserByUsernameOrEmail(username: String?, email: String?): AppUser? {
        log.info("Searching for user with username or email: $username, $email")
        val user = userRepository.findByUsernameOrEmail(username, email)
            ?: throw NotFoundException("User with username or email $username, $email not found")
        return user
    }
}
