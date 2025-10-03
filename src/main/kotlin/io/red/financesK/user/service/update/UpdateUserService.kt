package io.red.financesK.user.service.update

import io.red.financesK.auth.service.AuthService
import io.red.financesK.auth.service.PasswordService
import io.red.financesK.global.exception.ValidationException
import io.red.financesK.mail.service.MailService
import io.red.financesK.user.controller.request.UpdateUserRequest
import io.red.financesK.user.controller.response.GenericResponse
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import io.red.financesK.user.service.search.SearchUserService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UpdateUserService(
    private val searchUserServices: SearchUserService,
    private val userRepository: AppUserRepository,
    private val mailService: MailService,
    private val authService: AuthService,
    private val passwordService: PasswordService
) {

    private val log = LoggerFactory.getLogger(UpdateUserService::class.java)

    fun updateUser(userId: Int, request: UpdateUserRequest) {
        log.info("m=updateUser, action=Finding user with id: $userId")
        val user = searchUserServices.findUserById(userId)

        user.username = request.username.ifEmpty { user.username }
        user.email = if (request.email?.isNotEmpty() == true) request.email else user.email
        user.pathAvatar = if (request.pathAvatar?.isNotEmpty() == true) request.pathAvatar else user.pathAvatar
        user.updatedAt = Instant.now()

        log.info("m=updateUser, action=Updating user with id: $userId")
        saveUser(user)

    }

    fun resetPassword(request: HttpServletRequest, email: String): GenericResponse {
        val user = userRepository.findByEmail(email)
            ?: throw ValidationException("User with email $email not found")

        val token = authService.getTokenFromUserId(user.id!!.toLong())

        passwordService.createPasswordResetTokenForUser(user, token)
        mailService.sendMailToken(
            mailService.constructResetTokenEmail(
                request.contextPath, request.locale, token, user
            )
        )
        return GenericResponse(
            "If the email is registered, a password reset link will be sent.",
            null,
            request.locale
        )

    }

    fun saveUser(user: AppUser) {
        log.info("m=saveUser, action=Saving user with id: ${user.id}")
        userRepository.save(user)
    }

}
