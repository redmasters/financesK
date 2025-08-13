package io.red.financesK.category.service.create

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.red.financesK.category.controller.request.CreateCategoryRequest
import io.red.financesK.category.model.Category
import io.red.financesK.category.repository.CategoryRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class CreateCategoryServiceTest {

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var createCategoryService: CreateCategoryService

    @BeforeEach
    fun setUp() {
        categoryRepository = mockk()
        createCategoryService = CreateCategoryService(categoryRepository)
    }

    @Test
    @DisplayName("Deve criar categoria com sucesso quando todos os campos são fornecidos")
    fun `should create category successfully when all fields are provided`() {
        // Given
        val request = CreateCategoryRequest(
            name = "Alimentação",
            icon = "🍔",
            color = "#FF5722",
            parentId = null
        )

        val savedCategory = Category(
            id = 1,
            name = "Alimentação",
            icon = "🍔",
            color = "#FF5722",
            parentId = null
        )

        every { categoryRepository.save(any<Category>()) } returns savedCategory

        // When
        val result = createCategoryService.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(1, result.id)
        assertEquals("Category created successfully", result.message)
        assertNotNull(result.createdAt)

        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve criar categoria com apenas nome obrigatório")
    fun `should create category with only required name field`() {
        // Given
        val request = CreateCategoryRequest(
            name = "Transporte",
            icon = null,
            color = null,
            parentId = null
        )

        val savedCategory = Category(
            id = 2,
            name = "Transporte",
            icon = null,
            color = null,
            parentId = null
        )

        every { categoryRepository.save(any<Category>()) } returns savedCategory

        // When
        val result = createCategoryService.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(2, result.id)
        assertEquals("Category created successfully", result.message)
        assertNotNull(result.createdAt)

        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve criar subcategoria com categoria pai")
    fun `should create subcategory with parent category`() {
        // Given
        val request = CreateCategoryRequest(
            name = "Restaurantes",
            icon = "🍽️",
            color = "#FF5722",
            parentId = 1 // Subcategoria de "Alimentação"
        )

        val savedCategory = Category(
            id = 3,
            name = "Restaurantes",
            icon = "🍽️",
            color = "#FF5722",
            parentId = 1
        )

        every { categoryRepository.save(any<Category>()) } returns savedCategory

        // When
        val result = createCategoryService.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(3, result.id)
        assertEquals("Category created successfully", result.message)
        assertNotNull(result.createdAt)

        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve criar categoria com nome no limite máximo de caracteres (boundary)")
    fun `should create category with maximum character limit - boundary`() {
        // Given
        val maxLengthName = "A".repeat(100) // Limite máximo conforme validação
        val request = CreateCategoryRequest(
            name = maxLengthName,
            icon = "🏠",
            color = "#2196F3",
            parentId = null
        )

        val savedCategory = Category(
            id = 4,
            name = maxLengthName,
            icon = "🏠",
            color = "#2196F3",
            parentId = null
        )

        every { categoryRepository.save(any<Category>()) } returns savedCategory

        // When
        val result = createCategoryService.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(4, result.id)
        assertEquals("Category created successfully", result.message)
        assertNotNull(result.createdAt)

        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve criar categoria com ícone no limite máximo de caracteres (boundary)")
    fun `should create category with maximum icon length - boundary`() {
        // Given
        val maxLengthIcon = "🎯".repeat(25) // 50 caracteres (cada emoji = 2 chars)
        val request = CreateCategoryRequest(
            name = "Investimentos",
            icon = maxLengthIcon,
            color = "#4CAF50",
            parentId = null
        )

        val savedCategory = Category(
            id = 5,
            name = "Investimentos",
            icon = maxLengthIcon,
            color = "#4CAF50",
            parentId = null
        )

        every { categoryRepository.save(any<Category>()) } returns savedCategory

        // When
        val result = createCategoryService.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(5, result.id)
        assertEquals("Category created successfully", result.message)
        assertNotNull(result.createdAt)

        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve criar categoria com cor hexadecimal válida (boundary)")
    fun `should create category with valid hex color - boundary`() {
        // Given
        val request = CreateCategoryRequest(
            name = "Saúde",
            icon = "🏥",
            color = "#FF0000", // Cor hexadecimal de 7 caracteres
            parentId = null
        )

        val savedCategory = Category(
            id = 6,
            name = "Saúde",
            icon = "🏥",
            color = "#FF0000",
            parentId = null
        )

        every { categoryRepository.save(any<Category>()) } returns savedCategory

        // When
        val result = createCategoryService.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(6, result.id)
        assertEquals("Category created successfully", result.message)
        assertNotNull(result.createdAt)

        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve criar categoria com nome mínimo de um caractere (boundary)")
    fun `should create category with minimum name length - boundary`() {
        // Given
        val request = CreateCategoryRequest(
            name = "A", // Nome mínimo
            icon = "📝",
            color = "#9C27B0",
            parentId = null
        )

        val savedCategory = Category(
            id = 7,
            name = "A",
            icon = "📝",
            color = "#9C27B0",
            parentId = null
        )

        every { categoryRepository.save(any<Category>()) } returns savedCategory

        // When
        val result = createCategoryService.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(7, result.id)
        assertEquals("Category created successfully", result.message)
        assertNotNull(result.createdAt)

        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve criar categoria com parentId zero (boundary)")
    fun `should create category with zero parentId - boundary`() {
        // Given
        val request = CreateCategoryRequest(
            name = "Categoria Especial",
            icon = "⭐",
            color = "#FFC107",
            parentId = 0
        )

        val savedCategory = Category(
            id = 8,
            name = "Categoria Especial",
            icon = "⭐",
            color = "#FFC107",
            parentId = 0
        )

        every { categoryRepository.save(any<Category>()) } returns savedCategory

        // When
        val result = createCategoryService.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(8, result.id)
        assertEquals("Category created successfully", result.message)
        assertNotNull(result.createdAt)

        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve criar categoria com parentId negativo (boundary)")
    fun `should create category with negative parentId - boundary`() {
        // Given
        val request = CreateCategoryRequest(
            name = "Categoria Teste",
            icon = "🧪",
            color = "#607D8B",
            parentId = -1
        )

        val savedCategory = Category(
            id = 9,
            name = "Categoria Teste",
            icon = "🧪",
            color = "#607D8B",
            parentId = -1
        )

        every { categoryRepository.save(any<Category>()) } returns savedCategory

        // When
        val result = createCategoryService.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(9, result.id)
        assertEquals("Category created successfully", result.message)
        assertNotNull(result.createdAt)

        verify { categoryRepository.save(any<Category>()) }
    }

    @Test
    @DisplayName("Deve criar categoria com caracteres especiais no nome")
    fun `should create category with special characters in name`() {
        // Given
        val request = CreateCategoryRequest(
            name = "Lazer & Entretenimento",
            icon = "🎮",
            color = "#E91E63",
            parentId = null
        )

        val savedCategory = Category(
            id = 10,
            name = "Lazer & Entretenimento",
            icon = "🎮",
            color = "#E91E63",
            parentId = null
        )

        every { categoryRepository.save(any<Category>()) } returns savedCategory

        // When
        val result = createCategoryService.execute(request)

        // Then
        assertNotNull(result)
        assertEquals(10, result.id)
        assertEquals("Category created successfully", result.message)
        assertNotNull(result.createdAt)

        verify { categoryRepository.save(any<Category>()) }
    }
}
