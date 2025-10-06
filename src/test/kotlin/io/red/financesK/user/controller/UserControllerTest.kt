package io.red.financesK.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.red.financesK.auth.controller.response.AuthUserResponse
import io.red.financesK.config.TestSecurityConfig
import io.red.financesK.user.controller.request.CreateUserRequest
import io.red.financesK.user.controller.request.UpdateUserRequest
import io.red.financesK.user.service.create.CreateUserService
import io.red.financesK.user.service.update.UpdateUserService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(UserController::class)
@Import(TestSecurityConfig::class)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var createUserService: CreateUserService

    @MockitoBean
    private lateinit var updateUserService: UpdateUserService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `createUser should return created status and user response`() {
        // Given
        val request = CreateUserRequest(
            username = "testuser",
            email = "test@example.com",
            password = "password123"
        )
        val userResponse = AuthUserResponse(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            token = "jwt-token"
        )

        `when`(createUserService.execute(request)).thenReturn(userResponse)

        // When & Then
        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.token").value("jwt-token"))
    }

    @Test
    fun `updateUser should return success message`() {
        // Given
        val userId = 1
        val request = UpdateUserRequest(
            username = "updateduser",
            email = "updated@example.com",
            oldPassword = "oldpass",
            newPassword = "newpass",
            confirmPassword = "newpass",
            pathAvatar = "avatar.png"
        )

        // When & Then
        mockMvc.perform(
            put("/api/v1/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(content().string("Success"))
    }

    @Test
    fun `resetPassword should return success message`() {
        // Given
        val email = "test@example.com"

        // When & Then
        mockMvc.perform(
            post("/api/v1/users/reset-password")
                .param("email", email)
        )
            .andExpect(status().isCreated)
            .andExpect(content().string("If the email is registered, a password reset link will be sent."))
    }
}
