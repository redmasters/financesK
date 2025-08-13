package io.red.financesK.category.service.update

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.red.financesK.category.controller.request.UpdateCategoryRequest
import io.red.financesK.category.model.Category
import io.red.financesK.category.repository.CategoryRepository
import io.red.financesK.global.exception.NotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class UpdateCategoryServiceTest {

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var updateCategoryService: UpdateCategoryService

    @BeforeEach
    fun setUp() {
        categoryRepository = mockk()
        updateCategoryService = UpdateCategoryService(categoryRepository)
    }

    @Test
    @DisplayName("Deve atualizar categoria com sucesso quando todos os campos são fornecidos")
    fun `should update category successfully when all fields are provided`() {
        // Given
        val categoryId = 1
        val existingCategory = Category(
            id = categoryId,
            name = "Alimentação",
            icon = "🍔",
            color = "#FF5722",
            parentId = null
        )

        val request = UpdateCategoryRequest(
            name = "Alimentação e Bebidas",
            icon = "🍽️",
            color = "#E91E63",
            parentId = 2
        )

        val updatedCategory = existingCategory.copy(
            name = "Alimentação e Bebidas",
            icon = "🍽️",
            color = "#E91E63",
            parentId = 2
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(existingCategory)
        every { categoryRepository.save(any<Category>()) } returns updatedCategory

        // When
        val result = updateCategoryService.execute(categoryId, request)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Category updated successfully", result.message)
        assertNotNull(result.updatedAt)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve atualizar apenas campos fornecidos (atualização parcial)")
    fun `should update only provided fields - partial update`() {
        // Given
        val categoryId = 2
        val existingCategory = Category(
            id = categoryId,
            name = "Transporte",
            icon = "🚗",
            color = "#2196F3",
            parentId = null
        )

        val request = UpdateCategoryRequest(
            name = "Transporte Público",
            icon = null,
            color = null,
            parentId = null
        )

        val updatedCategory = existingCategory.copy(
            name = "Transporte Público"
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(existingCategory)
        every { categoryRepository.save(any<Category>()) } returns updatedCategory

        // When
        val result = updateCategoryService.execute(categoryId, request)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertNotNull(result.updatedAt)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando categoria não existe")
    fun `should throw NotFoundException when category does not exist`() {
        // Given
        val categoryId = 999
        val request = UpdateCategoryRequest(
            name = "Nova Categoria"
        )

        every { categoryRepository.findById(categoryId) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<NotFoundException> {
            updateCategoryService.execute(categoryId, request)
        }

        assertEquals("Category not found with id: $categoryId", exception.message)
        verify { categoryRepository.findById(categoryId) }
        verify(exactly = 0) { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve atualizar categoria com nome no limite máximo de caracteres (boundary)")
    fun `should update category with maximum name length - boundary`() {
        // Given
        val categoryId = 3
        val existingCategory = Category(
            id = categoryId,
            name = "Categoria Original",
            icon = "📝",
            color = "#9C27B0",
            parentId = null
        )

        val maxLengthName = "A".repeat(100)
        val request = UpdateCategoryRequest(
            name = maxLengthName
        )

        val updatedCategory = existingCategory.copy(
            name = maxLengthName
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(existingCategory)
        every { categoryRepository.save(any<Category>()) } returns updatedCategory

        // When
        val result = updateCategoryService.execute(categoryId, request)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertNotNull(result.updatedAt)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve atualizar categoria sem fornecer nenhum campo (request vazio)")
    fun `should handle empty update request - no fields provided`() {
        // Given
        val categoryId = 4
        val existingCategory = Category(
            id = categoryId,
            name = "Categoria Inalterada",
            icon = "🔄",
            color = "#607D8B",
            parentId = null
        )

        val request = UpdateCategoryRequest()

        val updatedCategory = existingCategory.copy()

        every { categoryRepository.findById(categoryId) } returns Optional.of(existingCategory)
        every { categoryRepository.save(any<Category>()) } returns updatedCategory

        // When
        val result = updateCategoryService.execute(categoryId, request)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertNotNull(result.updatedAt)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve atualizar categoria com parentId zero (boundary)")
    fun `should update category with zero parentId - boundary`() {
        // Given
        val categoryId = 5
        val existingCategory = Category(
            id = categoryId,
            name = "Categoria Filha",
            icon = "👶",
            color = "#4CAF50",
            parentId = 10
        )

        val request = UpdateCategoryRequest(
            parentId = 0
        )

        val updatedCategory = existingCategory.copy(
            parentId = 0
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(existingCategory)
        every { categoryRepository.save(any<Category>()) } returns updatedCategory

        // When
        val result = updateCategoryService.execute(categoryId, request)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertNotNull(result.updatedAt)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve atualizar categoria com parentId negativo (boundary)")
    fun `should update category with negative parentId - boundary`() {
        // Given
        val categoryId = 6
        val existingCategory = Category(
            id = categoryId,
            name = "Categoria Teste",
            icon = "🧪",
            color = "#FF9800",
            parentId = null
        )

        val request = UpdateCategoryRequest(
            parentId = -1
        )

        val updatedCategory = existingCategory.copy(
            parentId = -1
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(existingCategory)
        every { categoryRepository.save(any<Category>()) } returns updatedCategory

        // When
        val result = updateCategoryService.execute(categoryId, request)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertNotNull(result.updatedAt)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve atualizar categoria com ícone no limite máximo de caracteres (boundary)")
    fun `should update category with maximum icon length - boundary`() {
        // Given
        val categoryId = 7
        val existingCategory = Category(
            id = categoryId,
            name = "Categoria Ícones",
            icon = "📱",
            color = "#3F51B5",
            parentId = null
        )

        val maxLengthIcon = "🎯".repeat(25) // 50 caracteres
        val request = UpdateCategoryRequest(
            icon = maxLengthIcon
        )

        val updatedCategory = existingCategory.copy(
            icon = maxLengthIcon
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(existingCategory)
        every { categoryRepository.save(any<Category>()) } returns updatedCategory

        // When
        val result = updateCategoryService.execute(categoryId, request)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertNotNull(result.updatedAt)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve atualizar categoria com cor hexadecimal válida (boundary)")
    fun `should update category with valid hex color - boundary`() {
        // Given
        val categoryId = 8
        val existingCategory = Category(
            id = categoryId,
            name = "Categoria Cores",
            icon = "🎨",
            color = "#000000",
            parentId = null
        )

        val request = UpdateCategoryRequest(
            color = "#FFFFFF"
        )

        val updatedCategory = existingCategory.copy(
            color = "#FFFFFF"
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(existingCategory)
        every { categoryRepository.save(any<Category>()) } returns updatedCategory

        // When
        val result = updateCategoryService.execute(categoryId, request)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertNotNull(result.updatedAt)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve atualizar categoria transformando em categoria filha")
    fun `should update category to become subcategory`() {
        // Given
        val categoryId = 9
        val parentId = 1
        val existingCategory = Category(
            id = categoryId,
            name = "Categoria Raiz",
            icon = "🌳",
            color = "#795548",
            parentId = null
        )

        val request = UpdateCategoryRequest(
            name = "Categoria Filha",
            parentId = parentId
        )

        val updatedCategory = existingCategory.copy(
            name = "Categoria Filha",
            parentId = parentId
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(existingCategory)
        every { categoryRepository.save(any<Category>()) } returns updatedCategory

        // When
        val result = updateCategoryService.execute(categoryId, request)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertNotNull(result.updatedAt)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.save(any<Category>()) }
    }
}
