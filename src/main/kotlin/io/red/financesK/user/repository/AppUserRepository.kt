package io.red.financesK.user.repository

import io.red.financesK.user.model.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AppUserRepository : JpaRepository<AppUser, Int> {
    fun findByUsername(username: String): AppUser?
    fun findByUsernameOrderByEmail(usernameEmail: String): AppUser?

    @Query(
        """
       select *
        from tbl_app_user
        where email = :email
        or username = :username
    """, nativeQuery = true
    )
    fun findByUsernameOrEmail(
        @Param("username") username: String?,
        @Param("email") email: String?
    ): AppUser?

    fun findByEmail(email: String): AppUser?
    fun existsByUsername(username: String): Boolean
    fun existsByUsernameOrEmail(username: String, email: String): Boolean
    fun existsByEmail(email: String): Boolean
}
