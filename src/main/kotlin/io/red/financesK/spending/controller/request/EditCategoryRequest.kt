package io.red.financesK.spending.controller.request

data class EditCategoryRequest(
    val name: String,
    val description: String? = null
)
