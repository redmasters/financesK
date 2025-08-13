package io.red.financesK.category.service.search

import io.mockk.every
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

class SearchCategoryServiceTest {

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var searchCategoryService: SearchCategoryService

    @BeforeEach
    fun setUp() {
        categoryRepository = mockk()
        searchCategoryService = SearchCategoryService(categoryRepository)
    }

    @Test
    @DisplayName("Deve buscar categoria por ID com sucesso")
    fun `should find category by ID successfully`() {
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

        // When
        val result = searchCategoryService.findCategoryById(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Alimentação", result.name)
        assertEquals("🍔", result.icon)
        assertEquals("#FF5722", result.color)
        assertNull(result.parentId)

        verify { categoryRepository.findById(categoryId) }
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando categoria não existe para findCategoryById")
    fun `should throw NotFoundException when category does not exist for findCategoryById`() {
        // Given
        val categoryId = 999

        every { categoryRepository.findById(categoryId) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<NotFoundException> {
            searchCategoryService.findCategoryById(categoryId)
        }

        assertEquals("Category with ID $categoryId not found", exception.message)
        verify { categoryRepository.findById(categoryId) }
    }

    @Test
    @DisplayName("Deve buscar categoria por ID e retornar CategoryResponse com sucesso")
    fun `should get category by ID and return CategoryResponse successfully`() {
        // Given
        val categoryId = 2
        val category = Category(
            id = categoryId,
            name = "Transporte",
            icon = "🚗",
            color = "#2196F3",
            parentId = 1
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(category)

        // When
        val result = searchCategoryService.getCategoryById(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Transporte", result.name)
        assertEquals("🚗", result.icon)
        assertEquals("#2196F3", result.color)
        assertEquals(1, result.parentId)

        verify { categoryRepository.findById(categoryId) }
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando categoria não existe para getCategoryById")
    fun `should throw NotFoundException when category does not exist for getCategoryById`() {
        // Given
        val categoryId = 888

        every { categoryRepository.findById(categoryId) } returns Optional.empty()

        // When & Then
        val exception = assertThrows<NotFoundException> {
            searchCategoryService.getCategoryById(categoryId)
        }

        assertEquals("Category not found with id: $categoryId", exception.message)
        verify { categoryRepository.findById(categoryId) }
    }

    @Test
    @DisplayName("Deve buscar todas as categorias com sucesso")
    fun `should get all categories successfully`() {
        // Given
        val categories = listOf(
            Category(1, "Alimentação", "🍔", "#FF5722", null),
            Category(2, "Transporte", "🚗", "#2196F3", null),
            Category(3, "Restaurantes", "🍽️", "#E91E63", 1)
        )

        every { categoryRepository.findAll() } returns categories

        // When
        val result = searchCategoryService.getAllCategories()

        // Then
        assertNotNull(result)
        assertEquals(3, result.size)

        assertEquals(1, result[0].id)
        assertEquals("Alimentação", result[0].name)
        assertEquals("🍔", result[0].icon)

        assertEquals(2, result[1].id)
        assertEquals("Transporte", result[1].name)
        assertEquals("🚗", result[1].icon)

        assertEquals(3, result[2].id)
        assertEquals("Restaurantes", result[2].name)
        assertEquals(1, result[2].parentId)

        verify { categoryRepository.findAll() }
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há categorias")
    fun `should return empty list when no categories exist`() {
        // Given
        every { categoryRepository.findAll() } returns emptyList()

        // When
        val result = searchCategoryService.getAllCategories()

        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty())

        verify { categoryRepository.findAll() }
    }

    @Test
    @DisplayName("Deve buscar categorias raiz com sucesso")
    fun `should get root categories successfully`() {
        // Given
        val rootCategories = listOf(
            Category(1, "Alimentação", "🍔", "#FF5722", null),
            Category(2, "Transporte", "🚗", "#2196F3", null),
            Category(5, "Saúde", "🏥", "#4CAF50", null)
        )

        every { categoryRepository.findRootCategories() } returns rootCategories

        // When
        val result = searchCategoryService.getRootCategories()

        // Then
        assertNotNull(result)
        assertEquals(3, result.size)

        // Verifica se todas são categorias raiz (parentId = null)
        result.forEach { category ->
            assertNull(category.parentId)
        }

        assertEquals("Alimentação", result[0].name)
        assertEquals("Transporte", result[1].name)
        assertEquals("Saúde", result[2].name)

        verify { categoryRepository.findRootCategories() }
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há categorias raiz")
    fun `should return empty list when no root categories exist`() {
        // Given
        every { categoryRepository.findRootCategories() } returns emptyList()

        // When
        val result = searchCategoryService.getRootCategories()

        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty())

        verify { categoryRepository.findRootCategories() }
    }

    @Test
    @DisplayName("Deve buscar subcategorias por ID da categoria pai com sucesso")
    fun `should get subcategories by parent ID successfully`() {
        // Given
        val parentId = 1
        val subcategories = listOf(
            Category(3, "Restaurantes", "🍽️", "#E91E63", parentId),
            Category(4, "Supermercado", "🛒", "#FF9800", parentId),
            Category(7, "Fast Food", "🍟", "#FFC107", parentId)
        )

        every { categoryRepository.findSubcategoriesByParentId(parentId) } returns subcategories

        // When
        val result = searchCategoryService.getSubcategoriesByParentId(parentId)

        // Then
        assertNotNull(result)
        assertEquals(3, result.size)

        // Verifica se todas têm o parentId correto
        result.forEach { category ->
            assertEquals(parentId, category.parentId)
        }

        assertEquals("Restaurantes", result[0].name)
        assertEquals("Supermercado", result[1].name)
        assertEquals("Fast Food", result[2].name)

        verify { categoryRepository.findSubcategoriesByParentId(parentId) }
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há subcategorias para o parentId")
    fun `should return empty list when no subcategories exist for parentId`() {
        // Given
        val parentId = 999
        every { categoryRepository.findSubcategoriesByParentId(parentId) } returns emptyList()

        // When
        val result = searchCategoryService.getSubcategoriesByParentId(parentId)

        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty())

        verify { categoryRepository.findSubcategoriesByParentId(parentId) }
    }

    @Test
    @DisplayName("Deve buscar categoria com campos nulos com sucesso")
    fun `should find category with null fields successfully`() {
        // Given
        val categoryId = 10
        val category = Category(
            id = categoryId,
            name = "Categoria Simples",
            icon = null,
            color = null,
            parentId = null
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(category)

        // When
        val result = searchCategoryService.getCategoryById(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Categoria Simples", result.name)
        assertNull(result.icon)
        assertNull(result.color)
        assertNull(result.parentId)

        verify { categoryRepository.findById(categoryId) }
    }

    @Test
    @DisplayName("Deve buscar categoria com ID zero (boundary)")
    fun `should find category with zero ID - boundary`() {
        // Given
        val categoryId = 0
        val category = Category(
            id = categoryId,
            name = "Categoria Zero",
            icon = "0️⃣",
            color = "#000000",
            parentId = null
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(category)

        // When
        val result = searchCategoryService.getCategoryById(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Categoria Zero", result.name)
        assertEquals("0️⃣", result.icon)
        assertEquals("#000000", result.color)

        verify { categoryRepository.findById(categoryId) }
    }

    @Test
    @DisplayName("Deve buscar categoria com parentId zero (boundary)")
    fun `should find subcategories with zero parentId - boundary`() {
        // Given
        val parentId = 0
        val subcategories = listOf(
            Category(11, "Sub Zero", "❄️", "#00BCD4", parentId)
        )

        every { categoryRepository.findSubcategoriesByParentId(parentId) } returns subcategories

        // When
        val result = searchCategoryService.getSubcategoriesByParentId(parentId)

        // Then
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(parentId, result[0].parentId)
        assertEquals("Sub Zero", result[0].name)

        verify { categoryRepository.findSubcategoriesByParentId(parentId) }
    }

    @Test
    @DisplayName("Deve buscar categoria com nome máximo de caracteres (boundary)")
    fun `should find category with maximum name length - boundary`() {
        // Given
        val categoryId = 12
        val maxLengthName = "A".repeat(100)
        val category = Category(
            id = categoryId,
            name = maxLengthName,
            icon = "📏",
            color = "#795548",
            parentId = null
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(category)

        // When
        val result = searchCategoryService.getCategoryById(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals(maxLengthName, result.name)
        assertEquals("📏", result.icon)

        verify { categoryRepository.findById(categoryId) }
    }

    @Test
    @DisplayName("Deve buscar categoria com caracteres especiais no nome")
    fun `should find category with special characters in name`() {
        // Given
        val categoryId = 13
        val category = Category(
            id = categoryId,
            name = "Lazer & Entretenimento - Categoria Especial!",
            icon = "🎮",
            color = "#9C27B0",
            parentId = null
        )

        every { categoryRepository.findById(categoryId) } returns Optional.of(category)

        // When
        val result = searchCategoryService.getCategoryById(categoryId)

        // Then
        assertNotNull(result)
        assertEquals(categoryId, result.id)
        assertEquals("Lazer & Entretenimento - Categoria Especial!", result.name)
        assertEquals("🎮", result.icon)

        verify { categoryRepository.findById(categoryId) }
    }
}
