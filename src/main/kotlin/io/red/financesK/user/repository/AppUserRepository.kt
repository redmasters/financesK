package io.red.financesK.user.repository

import io.red.financesK.user.model.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AppUserRepository : JpaRepository<AppUser, Int> {
    fun findByUsername(username: String): AppUser?
    fun findByUsernameOrderByEmail(usernameEmail: String): AppUser?
    fun findByEmail(email: String): AppUser?
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}
