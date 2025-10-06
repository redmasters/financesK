package io.red.financesK.auth.service

import io.red.financesK.auth.model.Authority
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import io.red.financesK.user.service.search.SearchUserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthorityService(
    private val userRepository: AppUserRepository,
    private val searchUserService: SearchUserService
) {
    private val log = LoggerFactory.getLogger(AuthorityService::class.java)

    /**
     * Concede uma authority a um usuário
     */
    fun grantAuthority(userId: Int, authority: Authority): AppUser {
        val user = searchUserService.findUserById(userId)

        if (user.authorities.contains(authority)) {
            log.info("User ${user.username} already has authority ${authority.value}")
            return user
        }

        user.authorities.add(authority)
        val savedUser = userRepository.save(user)

        log.info("Authority ${authority.value} granted to user ${user.username}")
        return savedUser
    }

    /**
     * Remove uma authority de um usuário
     */
    fun revokeAuthority(userId: Int, authority: Authority): AppUser {
        val user = searchUserService.findUserById(userId)

        if (!user.authorities.contains(authority)) {
            log.info("User ${user.username} doesn't have authority ${authority.value}")
            return user
        }

        // Não permitir remover a última authority USER de um usuário comum
        if (authority == Authority.USER && user.authorities.size == 1) {
            log.warn("Cannot remove the last USER authority from user ${user.username}")
            throw IllegalArgumentException("Cannot remove the last USER authority from a user")
        }

        user.authorities.remove(authority)
        val savedUser = userRepository.save(user)

        log.info("Authority ${authority.value} revoked from user ${user.username}")
        return savedUser
    }

    /**
     * Promove um usuário a ADMIN
     */
    fun promoteToAdmin(userId: Int): AppUser {
        val user = searchUserService.findUserById(userId)

        user.authorities.add(Authority.ADMIN)
        val savedUser = userRepository.save(user)

        log.info("User ${user.username} promoted to ADMIN")
        return savedUser
    }

    /**
     * Remove privilégios de ADMIN de um usuário
     */
    fun demoteFromAdmin(userId: Int): AppUser {
        val user = searchUserService.findUserById(userId)

        user.authorities.remove(Authority.ADMIN)
        val savedUser = userRepository.save(user)

        log.info("User ${user.username} demoted from ADMIN")
        return savedUser
    }

    /**
     * Verifica se um usuário tem uma authority específica
     */
    fun hasAuthority(userId: Int, authority: Authority): Boolean {
        val user = searchUserService.findUserById(userId)
        return user.authorities.contains(authority)
    }

    /**
     * Lista todas as authorities de um usuário
     */
    fun getUserAuthorities(userId: Int): Set<Authority> {
        val user = searchUserService.findUserById(userId)
        return user.authorities.toSet()
    }
}
