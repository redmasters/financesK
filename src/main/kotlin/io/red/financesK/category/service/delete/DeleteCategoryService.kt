package io.red.financesK.category.service.delete

import io.red.financesK.category.controller.response.DeleteCategoryResponse
import io.red.financesK.category.repository.CategoryRepository
import io.red.financesK.global.exception.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DeleteCategoryService(
    private val categoryRepository: CategoryRepository
) {
    private val log = LoggerFactory.getLogger(DeleteCategoryService::class.java)

    fun execute(categoryId: Int): DeleteCategoryResponse {
        log.info("m='execute', acao='deletando categoria', categoryId='$categoryId'")

        val category = categoryRepository.findById(categoryId)
            .orElseThrow {
                log.error("m='execute', acao='categoria não encontrada para deletar', categoryId='$categoryId'")
                NotFoundException("Category not found with id: $categoryId")
            }

        categoryRepository.delete(category)

        log.info("m='execute', acao='categoria deletada com sucesso', categoryId='$categoryId'")

        return DeleteCategoryResponse(
            id = categoryId
        )
    }
}
