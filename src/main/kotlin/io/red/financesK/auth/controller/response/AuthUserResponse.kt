package io.red.financesK.auth.controller.response

data class AuthUserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val token: String? = null
)
