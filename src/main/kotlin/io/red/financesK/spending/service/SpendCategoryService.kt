package io.red.financesK.spending.service

import io.red.financesK.spending.controller.request.CreateCategoryRequest
import io.red.financesK.spending.controller.request.EditCategoryRequest
import io.red.financesK.spending.controller.response.SpendCategoryResponse
import io.red.financesK.spending.model.SpendCategory
import io.red.financesK.spending.repository.SpendCategoryRepository
import org.springframework.stereotype.Service

@Service
class SpendCategoryService(private final val spendCategoryRepository: SpendCategoryRepository) {
    final val CATEGORY_NOT_FOUND = "Category not found"

    fun createCategory(request: CreateCategoryRequest): SpendCategory {
        return spendCategoryRepository.save(request.toModel())
    }

    fun editCategory(id: Long, request: EditCategoryRequest) {
        val category = spendCategoryRepository.findById(id)
            .orElseThrow { IllegalArgumentException(CATEGORY_NOT_FOUND) }
        category.name = request.name
        category.description = request.description
        spendCategoryRepository.save(category)
    }

    fun deleteCategory(id: Long) {
        val category = spendCategoryRepository.findById(id)
            .orElseThrow { IllegalArgumentException(CATEGORY_NOT_FOUND) }
        category.isDeleted = true
        spendCategoryRepository.save(category)
    }

    fun getCategoryById(id: Long): SpendCategoryResponse {
        val category =
            spendCategoryRepository.findById(id)
                .orElseThrow { IllegalArgumentException(CATEGORY_NOT_FOUND) }
        return SpendCategoryResponse(
            category.id,
            category.name,
            category.description
        )
    }

    fun getCategoryByName(name: String): SpendCategoryResponse {
        //TODO: Busca com nome parcial
        val category = spendCategoryRepository.findByName(name)
            ?: throw IllegalArgumentException(CATEGORY_NOT_FOUND)
        return SpendCategoryResponse(
            category.id,
            category.name,
            category.description
        )
    }

    fun getAllCategories(): List<SpendCategoryResponse> {
        return spendCategoryRepository.findByIsDeleted(false)
            .map {
                SpendCategoryResponse(
                    it.id,
                    it.name,
                    it.description
                )
            }
    }
}
