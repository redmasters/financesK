package io.red.financesK.auth.service

import io.red.financesK.user.model.AppUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val appUser: AppUser,
) : UserDetails {

    fun getAppUser(): AppUser {
        return appUser
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return emptyList()
    }

    override fun getPassword(): String {
        return appUser.passwordHash ?: ""
    }

    override fun getUsername(): String {
        return appUser.username ?: ""
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}
