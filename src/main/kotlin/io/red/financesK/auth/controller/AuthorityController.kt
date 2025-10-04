package io.red.financesK.auth.controller

import io.red.financesK.auth.model.Authority
import io.red.financesK.auth.service.AuthorityService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/authorities")
class AuthorityController(
    private val authorityService: AuthorityService
) {

    @PostMapping("/users/{userId}/grant/{authority}")
    @PreAuthorize("hasRole('ADMIN')")
    fun grantAuthority(
        @PathVariable userId: Int,
        @PathVariable authority: String
    ): ResponseEntity<Map<String, Any>> {
        val authorityEnum = Authority.valueOf(authority.uppercase())
        val user = authorityService.grantAuthority(userId, authorityEnum)

        return ResponseEntity.ok(mapOf(
            "message" to "Authority ${authorityEnum.value} granted successfully",
            "user" to mapOf(
                "id" to user.id,
                "username" to user.username,
                "authorities" to user.authorities.map { it.value }
            )
        ))
    }

    @DeleteMapping("/users/{userId}/revoke/{authority}")
    @PreAuthorize("hasRole('ADMIN')")
    fun revokeAuthority(
        @PathVariable userId: Int,
        @PathVariable authority: String
    ): ResponseEntity<Map<String, Any>> {
        val authorityEnum = Authority.valueOf(authority.uppercase())
        val user = authorityService.revokeAuthority(userId, authorityEnum)

        return ResponseEntity.ok(mapOf(
            "message" to "Authority ${authorityEnum.value} revoked successfully",
            "user" to mapOf(
                "id" to user.id,
                "username" to user.username,
                "authorities" to user.authorities.map { it.value }
            )
        ))
    }

    @PostMapping("/users/{userId}/promote-admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun promoteToAdmin(@PathVariable userId: Int): ResponseEntity<Map<String, Any>> {
        val user = authorityService.promoteToAdmin(userId)

        return ResponseEntity.ok(mapOf(
            "message" to "User promoted to ADMIN successfully",
            "user" to mapOf(
                "id" to user.id,
                "username" to user.username,
                "authorities" to user.authorities.map { it.value }
            )
        ))
    }

    @DeleteMapping("/users/{userId}/demote-admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun demoteFromAdmin(@PathVariable userId: Int): ResponseEntity<Map<String, Any>> {
        val user = authorityService.demoteFromAdmin(userId)

        return ResponseEntity.ok(mapOf(
            "message" to "User demoted from ADMIN successfully",
            "user" to mapOf(
                "id" to user.id,
                "username" to user.username,
                "authorities" to user.authorities.map { it.value }
            )
        ))
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.appUser.id")
    fun getUserAuthorities(@PathVariable userId: Int): ResponseEntity<Map<String, Any>> {
        val authorities = authorityService.getUserAuthorities(userId)

        return ResponseEntity.ok(mapOf(
            "userId" to userId,
            "authorities" to authorities.map { it.value }
        ))
    }

    @GetMapping("/available")
    fun getAvailableAuthorities(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "authorities" to Authority.values().map {
                mapOf(
                    "name" to it.value,
                    "description" to when(it) {
                        Authority.USER -> "Usuário comum com acesso básico"
                        Authority.ADMIN -> "Administrador com acesso total"
                        Authority.CHANGE_PASSWORD_PRIVILEGE -> "Privilégio temporário para alteração de senha"
                    }
                )
            }
        ))
    }
}
