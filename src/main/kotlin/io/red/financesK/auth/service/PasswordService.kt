package io.red.financesK.auth.service

import io.red.financesK.auth.model.PasswordResetToken
import io.red.financesK.auth.repository.PasswordResetTokenRepository
import io.red.financesK.user.controller.request.UpdateUserRequest
import io.red.financesK.user.controller.response.GenericResponse
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.service.update.UpdateUserService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*

@Service
class PasswordService(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val updateUserService: UpdateUserService
) {
    private val secureRandom = SecureRandom()
    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
    private val log = LoggerFactory.getLogger(PasswordService::class.java)

    fun encode(password: CharSequence): String {
        val encodedPassword = passwordEncoder.encode(password)
        log.info(encodedPassword)
        return encodedPassword
    }

    fun matches(rawPassword: CharSequence, encodedPassword: String): Boolean {
        val match = passwordEncoder.matches(rawPassword, encodedPassword)
        log.info(match.toString())
        return match

    }

    fun saltPassword(): String {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun createPasswordResetTokenForUser(user: AppUser, token: String): String {
        log.info("Creating password reset token for user: ${user.username}")
        val expirationDate = Date(System.currentTimeMillis() + PasswordResetToken().EXPIRATION)
        val myToken = PasswordResetToken(
            token = token,
            user = user,
            expiryDate = expirationDate
        )

        passwordResetTokenRepository.save(myToken)
        log.info("Password reset token created successfully")
        return token
    }

    fun validatePasswordResetToken(token: String): Boolean {
        if (isTokenFound(token)) {
            log.info("m='validatePasswordResetToken', acao='token encontrado', token='$token'")
            return true
        }
        log.info("m='validatePasswordResetToken', acao='token nao encontrado ou expirado', token='$token'")
        return false
    }

    fun savePassword(locale: Locale, @Valid request: UpdateUserRequest): GenericResponse {
        val token = request.token ?: ""
        if (validatePasswordResetToken(token)) {
            val passToken = passwordResetTokenRepository.findByToken(request.token ?: "")
            val user = passToken.get().user
            user?.let {
                it.passwordHash = encode(request.newPassword)
                it.passwordSalt = saltPassword()
                updateUserService.saveUser(it)

            }
            log.info("m='savePassword', acao='senha alterada com sucesso', user='${user?.username}'")
            return GenericResponse("auth.message.resetPasswordSuc", null, locale)
        }
        log.info("m='savePassword', acao='token invalido ou expirado', token='$token'")
        return GenericResponse("auth.message.invalid", null, locale)
    }

    fun isTokenFound(token: String): Boolean {
        val optionalToken = passwordResetTokenRepository.findByToken(token)
        return optionalToken.isPresent && !isTokenExpired(optionalToken.get())
    }

    fun isTokenExpired(passToken: PasswordResetToken): Boolean {
        val calendar = Calendar.getInstance()
        return passToken.expiryDate.before(calendar.time)
    }


}
