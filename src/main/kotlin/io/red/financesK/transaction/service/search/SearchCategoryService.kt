package io.red.financesK.transaction.service.search

import io.red.financesK.transaction.repository.CategoryRepository
import org.springframework.stereotype.Service

@Service
class SearchCategoryService(
    private val categoryRepository: CategoryRepository,
) {
    fun findCategoryById(categoryId: Int) =
        categoryRepository.findById(categoryId).orElseThrow {
            IllegalArgumentException("Category with ID $categoryId not found")
        }
}
