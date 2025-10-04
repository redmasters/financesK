package io.red.financesK.auth.service

import io.red.financesK.auth.model.Authority
import io.red.financesK.auth.model.PasswordResetToken
import io.red.financesK.auth.repository.PasswordResetTokenRepository
import io.red.financesK.user.controller.request.UpdateUserRequest
import io.red.financesK.user.controller.response.GenericResponse
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*

@Service
class PasswordService(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val appUserRepository: AppUserRepository
) {
    private val log = LoggerFactory.getLogger(PasswordService::class.java)
    private val passwordEncoder = BCryptPasswordEncoder()
    val EXPIRATION: Long = 60 * 24 * 1000 // 24 horas em milissegundos

    fun encode(rawPassword: String): String {
        log.info(passwordEncoder.encode(rawPassword))
        return passwordEncoder.encode(rawPassword)
    }

    fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }

    fun saltPassword(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { String.format("%02x", it) }
    }

    fun createPasswordResetTokenForUser(user: AppUser, token: String): String {
        val existingTokenOpt = passwordResetTokenRepository.findTopByUser_Id(user.id!!)

        if (existingTokenOpt.isPresent) {
            val existingToken = existingTokenOpt.get()
            if (!isTokenExpired(existingToken)) {
                log.info("m='createPasswordResetTokenForUser', acao='token existente ainda valido', user='${user.username}', token='${existingToken.token}'")
                return existingToken.token
            }
        }

        // Conceder privilégio temporário de reset de senha
        user.authorities.add(Authority.CHANGE_PASSWORD_PRIVILEGE)
        appUserRepository.save(user)
        log.info("m='createPasswordResetTokenForUser', acao='privilege concedido', user='${user.username}'")

        val myToken = PasswordResetToken(
            token = token,
            user = user,
            expiryDate = Date(System.currentTimeMillis() + EXPIRATION)
        )
        passwordResetTokenRepository.save(myToken)
        log.info("m='createPasswordResetTokenForUser', acao='novo token criado', user='${user.username}', token='$token'")
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

                // Remover o privilégio temporário após alterar a senha
                it.authorities.remove(Authority.CHANGE_PASSWORD_PRIVILEGE)

                appUserRepository.save(it)
                log.info("m='savePassword', acao='privilege removido apos alteracao', user='${it.username}'")
            }
            passwordResetTokenRepository.delete(passToken.get())
            log.info("m='savePassword', acao='senha alterada com sucesso', user='${user?.username}'")
            return GenericResponse("auth.message.resetPasswordSuc", null, locale)
        }
        log.info("m='savePassword', acao='token invalido ou expirado', token='$token'")
        return GenericResponse("auth.message.invalid", "error.token", locale)
    }

    fun getUserByPasswordResetToken(token: String): AppUser {
        val passToken = passwordResetTokenRepository.findByToken(token)
        return passToken.orElseThrow { RuntimeException("Token not found") }.user
            ?: throw RuntimeException("User not found for token")
    }

    fun isTokenFound(token: String): Boolean {
        val passToken = passwordResetTokenRepository.findByToken(token)
        return if (passToken.isPresent) {
            !isTokenExpired(passToken.get())
        } else {
            false
        }
    }

    fun isTokenExpired(passToken: PasswordResetToken): Boolean {
        val now = Date()
        return passToken.expiryDate.before(now)
    }
}
