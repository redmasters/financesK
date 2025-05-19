package io.red.financesK.spending.service

import io.red.financesK.spending.controller.request.CreateCategoryRequest
import io.red.financesK.spending.controller.response.SpendCategoryResponse
import io.red.financesK.spending.model.SpendCategory
import io.red.financesK.spending.repository.SpendCategoryRepository
import org.springframework.stereotype.Service

@Service
class SpendCategoryService(private final val spendCategoryRepository: SpendCategoryRepository) {

    fun createCategory(request: CreateCategoryRequest): SpendCategory {
        return spendCategoryRepository.save(request.toModel())
    }

    fun getAllCategories(): List<SpendCategoryResponse> {
        val categories = spendCategoryRepository.findAll()
        return categories.map {
            SpendCategoryResponse(
                it.id,
                it.name,
                it.description
            )
        }
    }
}
