package io.red.financesK.user.controller.response

import java.util.Locale

class GenericResponse {
    var message: String? = null
    var error: String? = null
    var locale: String? = null

    constructor(message: String?, error: String?, locale: Locale) {
        this.message = message
    }
}
