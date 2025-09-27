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
    var username: String?,

    @Column(name = "email", nullable = false, unique = true)
    var email: String?,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String?,

    @Column(name = "password_salt", nullable = true)
    var passwordSalt: String? = null,

    @Column(name = "path_avatar", nullable = false)
    var pathAvatar: String? = null,

    @Column(name = "created_at")
    var createdAt: Instant? = null,

    @Column(name = "updated_at")
    var updatedAt: Instant? = null
)
