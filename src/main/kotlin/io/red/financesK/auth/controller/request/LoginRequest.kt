package io.red.financesK.auth.controller.request

data class LoginRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String
)
