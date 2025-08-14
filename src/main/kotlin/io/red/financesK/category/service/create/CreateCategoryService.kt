package io.red.financesK.category.service.create

import io.red.financesK.category.controller.request.CreateCategoryRequest
import io.red.financesK.category.controller.response.CreateCategoryResponse
import io.red.financesK.category.model.Category
import io.red.financesK.category.repository.CategoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CreateCategoryService(
    private val categoryRepository: CategoryRepository
) {
    private val log = LoggerFactory.getLogger(CreateCategoryService::class.java)

    fun execute(request: CreateCategoryRequest): CreateCategoryResponse {
        log.info("m='execute', acao='criando categoria', request='$request'")
        val parentCategory = request.parentId?.let { parentId ->
            categoryRepository.findById(parentId).orElseThrow {
                IllegalArgumentException("Parent category with ID $parentId not found")
            }
        }

        val category = Category(
            name = request.name,
            icon = request.icon,
            color = request.color,
            parent = parentCategory
        )

        val savedCategory = categoryRepository.save(category)

        log.info("m='execute', acao='categoria criada com sucesso', categoryId='${savedCategory.id}'")

        return CreateCategoryResponse(
            id = savedCategory.id!!
        )
    }
}
