package io.red.financesK.category.service.delete

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import io.red.financesK.category.model.Category
import io.red.financesK.category.repository.CategoryRepository
import io.red.financesK.global.exception.NotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class DeleteCategoryServiceTest {

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var deleteCategoryService: DeleteCategoryService

    @BeforeEach
    fun setUp() {
        categoryRepository = mockk()
        deleteCategoryService = DeleteCategoryService(categoryRepository)
    }

    @Test
    @DisplayName("Deve deletar categoria com sucesso")
    fun `should delete category successfully`() {
        // Given
        val categoryId = 1
        val category = Category(
            id = categoryId,
            name = "Alimentação",
            icon = "🍔",
            color = "#FF5722",
            parentId = null
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(category)
        justRun { categoryRepository.delete(category) }

        // When
        val result = deleteCategoryService.execute(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Category deleted successfully", result.message)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.delete(category) }
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando categoria não existe")
    fun `should throw NotFoundException when category does not exist`() {
        // Given
        val categoryId = 999

        every { categoryRepository.findById(categoryId) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<NotFoundException> {
            deleteCategoryService.execute(categoryId)
        }

        assertEquals("Category not found with id: $categoryId", exception.message)
        verify { categoryRepository.findById(categoryId) }
        verify(exactly = 0) { categoryRepository.delete(any()) }
    }

    @Test
    @DisplayName("Deve deletar categoria pai com sucesso")
    fun `should delete parent category successfully`() {
        // Given
        val categoryId = 2
        val parentCategory = Category(
            id = categoryId,
            name = "Categoria Pai",
            icon = "👨‍👩‍👧‍👦",
            color = "#2196F3",
            parentId = null
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(parentCategory)
        justRun { categoryRepository.delete(parentCategory) }

        // When
        val result = deleteCategoryService.execute(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Category deleted successfully", result.message)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.delete(parentCategory) }
    }

    @Test
    @DisplayName("Deve deletar subcategoria com sucesso")
    fun `should delete subcategory successfully`() {
        // Given
        val categoryId = 3
        val subcategory = Category(
            id = categoryId,
            name = "Subcategoria",
            icon = "👶",
            color = "#4CAF50",
            parentId = 1
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(subcategory)
        justRun { categoryRepository.delete(subcategory) }

        // When
        val result = deleteCategoryService.execute(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Category deleted successfully", result.message)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.delete(subcategory) }
    }

    @Test
    @DisplayName("Deve deletar categoria com ID zero (boundary)")
    fun `should delete category with zero ID - boundary`() {
        // Given
        val categoryId = 0
        val category = Category(
            id = categoryId,
            name = "Categoria Zero",
            icon = "0️⃣",
            color = "#607D8B",
            parentId = null
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(category)
        justRun { categoryRepository.delete(category) }

        // When
        val result = deleteCategoryService.execute(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Category deleted successfully", result.message)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.delete(category) }
    }

    @Test
    @DisplayName("Deve deletar categoria com valores extremos")
    fun `should delete category with extreme values`() {
        // Given
        val categoryId = Int.MAX_VALUE
        val category = Category(
            id = categoryId,
            name = "Categoria Extrema",
            icon = "🚀",
            color = "#E91E63",
            parentId = -1
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(category)
        justRun { categoryRepository.delete(category) }

        // When
        val result = deleteCategoryService.execute(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Category deleted successfully", result.message)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.delete(category) }
    }

    @Test
    @DisplayName("Deve deletar categoria com nome máximo de caracteres")
    fun `should delete category with maximum name length`() {
        // Given
        val categoryId = 6
        val maxLengthName = "A".repeat(100)
        val category = Category(
            id = categoryId,
            name = maxLengthName,
            icon = "📏",
            color = "#FF9800",
            parentId = null
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(category)
        justRun { categoryRepository.delete(category) }

        // When
        val result = deleteCategoryService.execute(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Category deleted successfully", result.message)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.delete(category) }
    }

    @Test
    @DisplayName("Deve deletar categoria com campos nulos")
    fun `should delete category with null fields`() {
        // Given
        val categoryId = 7
        val category = Category(
            id = categoryId,
            name = "Categoria Simples",
            icon = null,
            color = null,
            parentId = null
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(category)
        justRun { categoryRepository.delete(category) }

        // When
        val result = deleteCategoryService.execute(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Category deleted successfully", result.message)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.delete(category) }
    }

    @Test
    @DisplayName("Deve deletar categoria com caracteres especiais no nome")
    fun `should delete category with special characters in name`() {
        // Given
        val categoryId = 8
        val category = Category(
            id = categoryId,
            name = "Lazer & Entretenimento - Categoria Especial!",
            icon = "🎮",
            color = "#9C27B0",
            parentId = null
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(category)
        justRun { categoryRepository.delete(category) }

        // When
        val result = deleteCategoryService.execute(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Category deleted successfully", result.message)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.delete(category) }
    }

    @Test
    @DisplayName("Deve deletar categoria com emojis múltiplos no ícone")
    fun `should delete category with multiple emojis in icon`() {
        // Given
        val categoryId = 9
        val category = Category(
            id = categoryId,
            name = "Categoria com Emojis",
            icon = "🏠🏢🏭🏗️🏰",
            color = "#795548",
            parentId = null
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(category)
        justRun { categoryRepository.delete(category) }

        // When
        val result = deleteCategoryService.execute(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Category deleted successfully", result.message)

        verify { categoryRepository.findById(categoryId) }
        verify { categoryRepository.delete(category) }
    }
}
