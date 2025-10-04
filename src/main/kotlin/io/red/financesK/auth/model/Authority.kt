package io.red.financesK.auth.model

enum class Authority(val value: String) {
    USER("USER"),
    ADMIN("ADMIN"),
    CHANGE_PASSWORD_PRIVILEGE("CHANGE_PASSWORD_PRIVILEGE")
}
