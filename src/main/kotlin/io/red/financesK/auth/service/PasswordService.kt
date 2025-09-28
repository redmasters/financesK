package io.red.financesK.auth.service

import org.slf4j.Logger
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class PasswordService {
    private val secureRandom = SecureRandom()
    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
    private val log: Logger = org.slf4j.LoggerFactory.getLogger(PasswordService::class.java)

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

}
