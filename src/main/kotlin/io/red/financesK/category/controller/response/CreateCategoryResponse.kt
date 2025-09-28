package io.red.financesK.category.controller.response

import java.time.Instant

data class CreateCategoryResponse(
    val id: Int,
    val message: String = "Category created successfully",
    val createdAt: Instant? = Instant.now()
)
