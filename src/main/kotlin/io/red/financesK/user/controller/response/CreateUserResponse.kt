package io.red.financesK.user.controller.response

import java.time.Instant

// Response DTO for user creation
class CreateUserResponse(
    val id: Int,
    val username: String?,
    val email: String?,
    val createdAt: Instant
)

