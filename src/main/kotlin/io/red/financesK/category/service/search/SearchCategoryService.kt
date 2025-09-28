package io.red.financesK.category.service.search

import io.red.financesK.category.controller.response.CategoryResponse
import io.red.financesK.category.model.Category
import io.red.financesK.category.repository.CategoryRepository
import io.red.financesK.global.exception.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SearchCategoryService(
    private val categoryRepository: CategoryRepository,
) {
    private val log = LoggerFactory.getLogger(SearchCategoryService::class.java)

    fun findCategoryById(categoryId: Int?): Category {
        log.info("m='findCategoryById', acao='buscando categoria por id', categoryId='$categoryId'")

        return categoryRepository.findById(categoryId!!).orElseThrow {
            log.error("m='findCategoryById', acao='categoria não encontrada', categoryId='$categoryId'")
            NotFoundException("Category with ID $categoryId not found")
        }
    }

    fun getCategoryById(categoryId: Int): CategoryResponse {
        log.info("m='getCategoryById', acao='buscando categoria por id', categoryId='$categoryId'")

        val category = categoryRepository.findById(categoryId)
            .orElseThrow {
                log.error("m='getCategoryById', acao='categoria não encontrada', categoryId='$categoryId'")
                NotFoundException("Category not found with id: $categoryId")
            }

        log.info("m='getCategoryById', acao='categoria encontrada com sucesso', categoryId='$categoryId'")

        return CategoryResponse(
            id = category.id!!,
            name = category.name ?: "",
            icon = category.icon,
            color = category.color,
            parentId = category.parent?.id
        )
    }

    fun getAllCategories(): List<CategoryResponse> {
        log.info("m='getAllCategories', acao='buscando todas as categorias'")

        val categories = categoryRepository.findAll()

        log.info("m='getAllCategories', acao='categorias encontradas', total='${categories.size}'")

        return categories.map { category ->
            CategoryResponse(
                id = category.id!!,
                name = category.name ?: "",
                icon = category.icon,
                color = category.color,
                parentId = category.parent?.id
            )
        }
    }

    fun getRootCategories(): List<CategoryResponse> {
        log.info("m='getRootCategories', acao='buscando categorias raiz'")

        val categories = categoryRepository.findRootCategories()

        log.info("m='getRootCategories', acao='categorias raiz encontradas', total='${categories.size}'")

        return categories.map { category ->
            CategoryResponse(
                id = category.id!!,
                name = category.name ?: "",
                icon = category.icon,
                color = category.color,
                parentId = category.parent?.id
            )
        }
    }

    fun getSubcategoriesByParentId(parentId: Int): List<CategoryResponse> {
        log.info("m='getSubcategoriesByParentId', acao='buscando subcategorias', parentId='$parentId'")

        val categories = categoryRepository.findSubcategoriesByParentId(parentId)

        log.info("m='getSubcategoriesByParentId', acao='subcategorias encontradas', parentId='$parentId', total='${categories.size}'")

        return categories.map { category ->
            CategoryResponse(
                id = category.id!!,
                name = category.name ?: "",
                icon = category.icon,
                color = category.color,
                parentId = category.parent?.id
            )
        }
    }
}
