package io.red.financesK.user.service.create

import io.red.financesK.global.exception.ValidationException
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import io.red.financesK.user.controller.request.CreateUserRequest
import io.red.financesK.user.controller.response.CreateUserResponse
import org.springframework.stereotype.Service
import java.time.Instant
import org.slf4j.LoggerFactory

@Service
class CreateUserService(
    private val appUserRepository: AppUserRepository
) {
    private val log = LoggerFactory.getLogger(CreateUserService::class.java)

    fun execute(request: CreateUserRequest): CreateUserResponse {
        log.info("m='execute', acao='criando usuário', username='{}', email='{}'", request.username, request.email)
        if (request.username.length < 6) {
            log.info("m='execute', acao='username inválido', username='{}'", request.username)
            throw ValidationException("Username must be at least 6 characters long.")
        }
        if (request.passwordHash.length < 6) {
            log.info("m='execute', acao='senha inválida', username='{}'", request.username)
            throw ValidationException("Password must be at least 6 characters long.")
        }
        val user = AppUser(
            username = request.username,
            email = request.email,
            passwordHash = request.passwordHash,
            createdAt = Instant.now()
        )
        val saved = appUserRepository.save(user)
        log.info("m='execute', acao='usuário criado', id='{}'", saved.id)
        return CreateUserResponse(
            id = saved.id!!,
            username = saved.username,
            email = saved.email,
            createdAt = saved.createdAt!!
        )
    }
}
