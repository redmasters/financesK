package io.red.financesK.category.controller.response

import java.time.Instant

data class UpdateCategoryResponse(
    val id: Int,
    val message: String = "Category updated successfully",
    val updatedAt: Instant? = Instant.now()
)
