package io.red.financesK.user.service.update

import io.red.financesK.auth.service.PasswordService
import io.red.financesK.user.controller.request.UpdateUserRequest
import io.red.financesK.user.repository.AppUserRepository
import io.red.financesK.user.service.search.SearchUserService
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UpdateUserService(
    private val searchUserServices: SearchUserService,
    private val passwordService: PasswordService,
    private val userRepository: AppUserRepository
) {

    fun updateUser(userId: Int, request: UpdateUserRequest) {
        val user = searchUserServices.findUserById(userId);

        user.username = request.username
        user.email = request.email ?: user.email
        user.passwordHash = updatePassword(user.passwordHash, request.oldPassword, request.newPassword)
        user.passwordSalt = if (request.newPassword.isNotEmpty()) passwordService.saltPassword() else user.passwordSalt
        user.pathAvatar = request.pathAvatar
        user.updatedAt = Instant.now()

        userRepository.save(user)

    }

    private fun updatePassword(currentPasswordHash: String?, oldPassword: String, newPassword: String): String? {
        if (newPassword.isEmpty()) return currentPasswordHash

        if (oldPassword.isNotEmpty() && newPassword.isNotEmpty()) {

            val encodedOldPassword = passwordService.encode(oldPassword)
            val passMatch = passwordService.matches(oldPassword, encodedOldPassword)
            if (passMatch) {
                return passwordService.encode(newPassword)
            } else {
                throw IllegalArgumentException("Old password does not match")
            }
        }
        return currentPasswordHash

    }

}
