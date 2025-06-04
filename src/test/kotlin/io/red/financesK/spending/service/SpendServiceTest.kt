package io.red.financesK.spending.service

import io.mockk.every
import io.mockk.mockk
import io.red.financesK.spending.controller.request.FilterSpendRequest
import io.red.financesK.spending.enums.SpendStatus
import io.red.financesK.spending.model.Spend
import io.red.financesK.spending.model.SpendCategory
import io.red.financesK.spending.repository.SpendRepository
import io.red.financesK.spending.repository.SpendCategoryRepository
import io.red.financesK.spending.repository.SpendCustomRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class SpendServiceTest {

    private val spendRepository = mockk<SpendRepository>()
    private val spendCategoryRepository = mockk<SpendCategoryRepository>()
    private val spendCustomRepository = mockk<SpendCustomRepository>()
    private val spendService = SpendService(spendRepository, spendCategoryRepository, spendCustomRepository)

    @Test
    fun filterSpendBy_returnsMappedSpendResponses_whenSpendsFound() {
        val category = SpendCategory(1L, "Food")
        val spend = Spend(
            id = 1L,
            name = "Lunch",
            description = "Lunch at cafe",
            amount = BigDecimal.valueOf(12.50),
            dueDate = LocalDate.now().minusDays(1),
            category = category,
            isDue = true,
            isPaid = false,
            isRecurring = false,
            status = SpendStatus.PENDING
        )
        val request = FilterSpendRequest(
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            isPaid = false,
            isDue = true,
            categoryId = 1L
        )
        every {
            spendCustomRepository.filterSpendBy(request)

        } returns listOf(spend)

        val result = spendService.filterSpendBy(request)

        assertEquals(1, result.size)
        assertEquals("Lunch", result[0].name)
        assertEquals("Food", result[0].categoryName)
        assertTrue(result[0].isDue)
    }

    @Test
    fun filterSpendBy_returnsEmptyList_whenNoSpendsFound() {
        val request = FilterSpendRequest(
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            isPaid = false,
            isDue = true,
            categoryId = 1L
        )
        every {
            spendCustomRepository.filterSpendBy(request)
        } returns emptyList()

        val result = spendService.filterSpendBy(request)

        assertTrue(result.isEmpty())
    }

    @Test
    fun filterSpendBy_usesDefaultValues_whenRequestFieldsAreNull() {
        val request = FilterSpendRequest(
            startDate = null,
            endDate = null,
            isPaid = null,
            isDue = null,
            categoryId = null
        )
        every {
            spendCustomRepository.filterSpendBy(request)
        } returns emptyList()

        val result = spendService.filterSpendBy(request)

        assertTrue(result.isEmpty())
    }

    @Test
    fun filterSpendBy_dueDatePeriod() {
        val request = FilterSpendRequest(
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            isPaid = null,
            isDue = null,
            categoryId = null
        )
        every {
            spendCustomRepository.filterSpendBy(request)
        } returns emptyList()

        val result = spendService.filterSpendBy(request)

        assertTrue(result.isEmpty())

    }
}


