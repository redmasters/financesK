package io.red.financesK.category.service.update

import io.red.financesK.category.controller.request.UpdateCategoryRequest
import io.red.financesK.category.controller.response.UpdateCategoryResponse
import io.red.financesK.category.repository.CategoryRepository
import io.red.financesK.global.exception.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UpdateCategoryService(
    private val categoryRepository: CategoryRepository
) {
    private val log = LoggerFactory.getLogger(UpdateCategoryService::class.java)

    fun execute(categoryId: Int, request: UpdateCategoryRequest): UpdateCategoryResponse {
        log.info("m='execute', acao='atualizando categoria', categoryId='$categoryId', request='$request'")

        val category = categoryRepository.findById(categoryId)
            .orElseThrow {
                log.error("m='execute', acao='categoria não encontrada', categoryId='$categoryId'")
                NotFoundException("Category not found with id: $categoryId")
            }

        // Create updated category with new values
        val updatedCategory = category.copy(
            name = request.name ?: category.name,
            icon = request.icon ?: category.icon,
            color = request.color ?: category.color,
            parentId = request.parentId ?: category.parentId
        )

        val savedCategory = categoryRepository.save(updatedCategory)

        log.info("m='execute', acao='categoria atualizada com sucesso', categoryId='$categoryId'")

        return UpdateCategoryResponse(
            id = savedCategory.id!!
        )
    }
}
