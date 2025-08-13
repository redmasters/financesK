package io.red.financesK.category.controller.response

data class DeleteCategoryResponse(
    val id: Int,
    val message: String = "Category deleted successfully"
)
