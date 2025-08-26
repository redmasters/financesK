package io.red.financesK.user.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "tbl_app_user")
data class AppUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,

    @Column(name = "username", nullable = false, unique = true)
    val username: String?,

    @Column(name = "email", nullable = false, unique = true)
    val email: String?,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String?,

    @Column(name = "path_avatar", nullable = false)
    val pathAvatar: String? = null,

    @Column(name = "created_at")
    val createdAt: Instant? = null
)
