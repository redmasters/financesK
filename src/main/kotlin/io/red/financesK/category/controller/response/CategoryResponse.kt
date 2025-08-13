package io.red.financesK.category.controller.response

data class CategoryResponse(
    val id: Int,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val parentId: Int? = null
)
