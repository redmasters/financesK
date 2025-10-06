package io.red.financesK.user.controller.request

data class PasswordRequest(
    val oldPassword: String = "",
    val newPassword: String,
    val token: String = ""
)
