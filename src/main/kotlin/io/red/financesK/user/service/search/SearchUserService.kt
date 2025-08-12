package io.red.financesK.user.service.search

import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.springframework.stereotype.Service

@Service
class SearchUserService(
    private val userRepository: AppUserRepository
) {
    fun findUserById(userId: Int): AppUser {
        return userRepository.findById(userId)
            .orElseThrow {
                IllegalArgumentException("User with ID $userId not found")
            }
    }
}
