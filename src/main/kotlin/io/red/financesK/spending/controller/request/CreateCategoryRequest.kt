package io.red.financesK.spending.controller.request

import io.red.financesK.spending.model.SpendCategory

data class CreateCategoryRequest(
    val name: String,
    val description: String? = null
) {
    fun toModel(): SpendCategory {
        return SpendCategory(
            name = name,
            description = description
        )
    }
}
