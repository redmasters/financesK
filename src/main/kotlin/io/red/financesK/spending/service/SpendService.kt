package io.red.financesK.spending.service

import io.red.financesK.spending.controller.request.CreateSpendRequest
import io.red.financesK.spending.controller.response.SpendResponse
import io.red.financesK.spending.enums.SpendStatus
import io.red.financesK.spending.model.Spend
import io.red.financesK.spending.repository.SpendCategoryRepository
import io.red.financesK.spending.repository.SpendRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SpendService(
    private val spendRepository: SpendRepository,
    private val spendCategoryRepository: SpendCategoryRepository
) {
    fun getAllSpends(): List<SpendResponse> {
        return spendRepository.findAll().map {
            SpendResponse(
                it.id,
                it.name,
                it.description,
                it.amount,
                it.dueDate.toString(),
                it.category.name,
                isDue(it.dueDate.toString()),
                it.isPaid,
                it.isRecurring,
                SpendStatus.valueOf(it.status.name).name
            )
        }
    }

    fun createSpend(request: CreateSpendRequest): Spend {
        val category = spendCategoryRepository.findById(request.categoryId).orElseThrow {
            IllegalArgumentException("Category with id ${request.categoryId} not found")
        }


        val spend = Spend(
            name = request.name,
            amount = request.amount,
            dueDate = LocalDate.parse(request.dueDate),
            category = category,
            description = request.description,
            isDue = isDue(request.dueDate),
            isPaid = request.isPaid,
            isRecurring = request.isRecurring
        )
        return spendRepository.save(spend)
    }

    companion object {
        fun isDue(dueDate: String): Boolean {
            return LocalDate.parse(dueDate).isBefore(LocalDate.now())
        }
    }

}
