package io.red.financesK.auth.model

import io.red.financesK.user.model.AppUser
import jakarta.persistence.*
import java.util.Date

@Entity
@Table(name = "tbl_password_reset_token")
data class PasswordResetToken(

    val EXPIRATION: Long = 1000 * 60 * 60, // 1 hour

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reset_token_id")
    val id: Long? = null,

    @Column(name = "reset_token")
    var token: String = "",

    @OneToOne(targetEntity = AppUser::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    var user: AppUser? = null,

    @Column(name = "expiry_date")
    var expiryDate: Date = Date()


)

