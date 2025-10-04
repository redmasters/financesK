package io.red.financesK.auth.repository

import io.red.financesK.auth.model.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {
    fun findByToken(token: String): Optional<PasswordResetToken>
    fun findTopByUser_Id(userId: Int): Optional<PasswordResetToken>
}
