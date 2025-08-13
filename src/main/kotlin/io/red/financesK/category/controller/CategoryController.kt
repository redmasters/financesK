package io.red.financesK.category.controller

import io.red.financesK.category.controller.request.CreateCategoryRequest
import io.red.financesK.category.controller.request.UpdateCategoryRequest
import io.red.financesK.category.controller.response.CategoryResponse
import io.red.financesK.category.controller.response.CreateCategoryResponse
import io.red.financesK.category.controller.response.DeleteCategoryResponse
import io.red.financesK.category.controller.response.UpdateCategoryResponse
import io.red.financesK.category.service.create.CreateCategoryService
import io.red.financesK.category.service.delete.DeleteCategoryService
import io.red.financesK.category.service.search.SearchCategoryService
import io.red.financesK.category.service.update.UpdateCategoryService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/categories")
@CrossOrigin(origins = ["*"])
class CategoryController(
    private val createCategoryService: CreateCategoryService,
    private val updateCategoryService: UpdateCategoryService,
    private val searchCategoryService: SearchCategoryService,
    private val deleteCategoryService: DeleteCategoryService
) {

    @PostMapping
    fun createCategory(@RequestBody @Valid request: CreateCategoryRequest): ResponseEntity<CreateCategoryResponse> {
        val response = createCategoryService.execute(request)
        return ResponseEntity.status(201).body(response)
    }

    @GetMapping("/{categoryId}")
    fun getCategoryById(@PathVariable categoryId: Int): ResponseEntity<CategoryResponse> {
        val response = searchCategoryService.getCategoryById(categoryId)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getAllCategories(): ResponseEntity<List<CategoryResponse>> {
        val response = searchCategoryService.getAllCategories()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/root")
    fun getRootCategories(): ResponseEntity<List<CategoryResponse>> {
        val response = searchCategoryService.getRootCategories()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/parent/{parentId}")
    fun getSubcategoriesByParentId(@PathVariable parentId: Int): ResponseEntity<List<CategoryResponse>> {
        val response = searchCategoryService.getSubcategoriesByParentId(parentId)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{categoryId}")
    fun updateCategory(
        @PathVariable categoryId: Int,
        @RequestBody @Valid request: UpdateCategoryRequest
    ): ResponseEntity<UpdateCategoryResponse> {
        val response = updateCategoryService.execute(categoryId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{categoryId}")
    fun deleteCategory(@PathVariable categoryId: Int): ResponseEntity<DeleteCategoryResponse> {
        val response = deleteCategoryService.execute(categoryId)
        return ResponseEntity.ok(response)
    }
}
