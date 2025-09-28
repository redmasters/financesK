package io.red.financesK.user.controller.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UpdateUserRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,
    @Email(message = "Email should be valid")
    val email: String?,
    val oldPassword: String,
    val newPassword: String,
    val pathAvatar: String? = "default_avatar.png"
)
