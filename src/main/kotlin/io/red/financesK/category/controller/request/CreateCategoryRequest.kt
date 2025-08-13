package io.red.financesK.category.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateCategoryRequest(
    @field:NotBlank(message = "Category name is required")
    @field:Size(max = 100, message = "Category name cannot exceed 100 characters")
    val name: String,

    @field:Size(max = 50, message = "Icon cannot exceed 50 characters")
    val icon: String? = null,

    @field:Size(max = 7, message = "Color must be a valid hex color")
    val color: String? = null,

    val parentId: Int? = null
)
