package io.red.financesK.spending.controller

import io.red.financesK.spending.controller.request.CreateCategoryRequest
import io.red.financesK.spending.controller.response.SpendCategoryResponse
import io.red.financesK.spending.model.SpendCategory
import io.red.financesK.spending.service.SpendCategoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("/api/spend-categories")
class SpendCategoryController(private val spendCategoryService: SpendCategoryService) {

    @GetMapping
    fun getAllCategories(): List<SpendCategoryResponse> {
        return spendCategoryService.getAllCategories();
    }

    @PostMapping
    fun createCategory(@RequestBody request: CreateCategoryRequest): ResponseEntity<SpendCategory?> {
        val savedCategory = spendCategoryService.createCategory(request)
        return ResponseEntity.created(
        URI("/${savedCategory.id}"))
        .body(savedCategory)
    }

}
