package io.red.financesK.user.service.create

import io.red.financesK.auth.controller.response.AuthUserResponse
import io.red.financesK.auth.service.AuthService
import io.red.financesK.auth.service.PasswordService
import io.red.financesK.user.controller.request.CreateUserRequest
import io.red.financesK.user.model.AppUser
import io.red.financesK.user.repository.AppUserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class CreateUserServiceTest {
    @Mock
    private lateinit var userRepository: AppUserRepository

    @Mock
    private lateinit var authService: AuthService

    @Mock
    private lateinit var passwordService: PasswordService

    @InjectMocks
    private lateinit var createUserService: CreateUserService

    private lateinit var userRequest: CreateUserRequest
    private lateinit var authUserResponse: AuthUserResponse
    private lateinit var user: AppUser

    @BeforeEach
    fun setUp() {
        userRequest = CreateUserRequest(
            username = "testuser",
            email = "test@test.com",
            password = "password",
            pathAvatar = null
        )
        user = AppUser(
            id = null,
            username = "testuser",
            email = "test@test.com",
            passwordHash = "hashedpassword",
            passwordSalt = "salt",
            pathAvatar = "default-avatar.png",
            createdAt = null,
            updatedAt = null
        )
        authUserResponse = AuthUserResponse(
            id = 1,
            username = "testuser",
            email = "test@test.com",
            token = "token"
        )
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    fun shouldCreateUserSuccessfully() {
        `when`(userRepository.existsByUsernameOrEmail("testuser", "test@test.com"))
            .thenReturn(false)
        `when`(passwordService.encode("password")).thenReturn("hashedpassword")
        `when`(passwordService.saltPassword()).thenReturn("salt")
        `when`(userRepository.save(Mockito.any(AppUser::class.java))).thenReturn(user.copy(id = 1))
        `when`(authService.getTokenFromUserId(1)).thenReturn("token")

        val result = createUserService.execute(userRequest)
        assertEquals(authUserResponse, result)
    }
}
