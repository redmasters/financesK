package io.red.financesK.auth.service

import io.red.financesK.user.service.search.SearchUserService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val searchUserService: SearchUserService
) : UserDetailsService {

    override fun loadUserByUsername(usernameEmail: String): UserDetails {
        val user = searchUserService.searchUserByUsername(usernameEmail)
            ?: throw IllegalArgumentException("User with username or email $usernameEmail not found")

        return CustomUserDetails(user)
    }
}
