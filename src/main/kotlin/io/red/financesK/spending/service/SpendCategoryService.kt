package io.red.financesK.spending.service

import io.red.financesK.spending.controller.request.CreateCategoryRequest
import io.red.financesK.spending.controller.request.EditCategoryRequest
import io.red.financesK.spending.controller.response.SpendCategoryResponse
import io.red.financesK.spending.model.SpendCategory
import io.red.financesK.spending.repository.SpendCategoryRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SpendCategoryService(private final val spendCategoryRepository: SpendCategoryRepository) {
    val logger: Logger = LoggerFactory.getLogger(SpendCategoryService::class.java)
    companion object {
        const val CATEGORY_NOT_FOUND = "Category not found"
    }
    fun createCategory(request: CreateCategoryRequest): SpendCategory {
        logger.info("m=createCategory - request: $request")
        return spendCategoryRepository.save(request.toModel())
    }

    fun editCategory(id: Long, request: EditCategoryRequest) {
        val category = spendCategoryRepository.findById(id)
            .orElseThrow { IllegalArgumentException(CATEGORY_NOT_FOUND) }
        category.name = request.name
        category.description = request.description
        logger.info("m=editCategory - request: $request")
        spendCategoryRepository.save(category)
    }

    fun deleteCategory(id: Long) {
        logger.info("m=deleteCategory - id: $id")
        val category = spendCategoryRepository.findById(id)
            .orElseThrow { IllegalArgumentException(CATEGORY_NOT_FOUND) }
        category.isDeleted = true
        spendCategoryRepository.save(category)
    }

    fun getCategoryById(id: Long): SpendCategoryResponse {
        logger.info("m=getCategoryById - id: $id")
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
        logger.info("m=getCategoryByName - name: $name")
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
                logger.info("m=getAllCategories - category: $it")
               SpendCategoryResponse(
                    it.id,
                    it.name,
                    it.description
                )
            }
    }
}
