package io.red.financesK.user.controller.request

// Request DTO for user creation
class CreateUserRequest(
    val username: String,
    val email: String,
    val passwordHash: String
)

