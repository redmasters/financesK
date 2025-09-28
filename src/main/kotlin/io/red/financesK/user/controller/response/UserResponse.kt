package io.red.financesK.user.controller.response

data class UserResponse(
    val id: Int?,
    val username: String?,
    val email: String?,
    val pathAvatar: String? = null
)
