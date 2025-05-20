package io.red.financesK.spending.service

import io.red.financesK.spending.controller.request.CreateSpendRequest
import io.red.financesK.spending.controller.request.EditSpendRequest
import io.red.financesK.spending.controller.response.SpendResponse
import io.red.financesK.spending.enums.SpendStatus
import io.red.financesK.spending.model.Spend
import io.red.financesK.spending.repository.SpendCategoryRepository
import io.red.financesK.spending.repository.SpendRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SpendService(
    private val spendRepository: SpendRepository,
    private val spendCategoryRepository: SpendCategoryRepository
) {
    val logger: Logger = LoggerFactory.getLogger(SpendService::class.java)

    fun getAllSpends(): List<SpendResponse> {
        val spends = spendRepository.findAll()

        logger.info("m=getAllSpends, spends: ${spends.size}")
        return spends.map {
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

    fun getSpendById(id: Long): SpendResponse {
        logger.info("m=getSpendById - id: $id")
        val spend = spendRepository.findById(id).orElseThrow {
            IllegalArgumentException("Spend with id $id not found")
        }
        return SpendResponse(
            spend.id,
            spend.name,
            spend.description,
            spend.amount,
            spend.dueDate.toString(),
            spend.category.name,
            isDue(spend.dueDate.toString()),
            spend.isPaid,
            spend.isRecurring,
            SpendStatus.valueOf(spend.status.name).name
        )
    }

    fun getSpendByCategoryId(categoryId: Long): List<SpendResponse> {
        logger.info("m=getSpendByCategoryId - categoryId: $categoryId")
        val spends = spendRepository.findSpendByCategory_Id(categoryId)
        return spends.map {
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

    fun getAllSpendStatus(): List<SpendStatus> {
        logger.info("m=getSpendStatuses")
        return SpendStatus.entries
    }

    fun getSpendStatusById(id: Long): SpendStatus {
        logger.info("m=getSpendStatusById - id: $id")
        return SpendStatus.entries.firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("Spend status with id $id not found")
    }

    fun getSpendByStatusByName(status: String): List<SpendResponse> {
        logger.info("m=getSpendByStatus - status: $status")
        val spends = spendRepository.findAll().filter { it.status.name == status }
        return spends.map {
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
        logger.info("m=createSpend - creating spend: $request")
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

    fun editSpend(id: Long, request: EditSpendRequest) {
        logger.info("m=editSpend - id: $id, request: $request")
        val spend = spendRepository.findById(id).orElseThrow {
            IllegalArgumentException("Spend with id $id not found")
        }
        val category = spendCategoryRepository.findById(request.categoryId).orElseThrow {
            IllegalArgumentException("Category with id ${request.categoryId} not found")
        }
        spend.name = request.name
        spend.amount = request.amount
        spend.dueDate = LocalDate.parse(request.dueDate)
        spend.category = category
        spend.description = request.description
        spend.isDue = isDue(request.dueDate)
        spend.isPaid = request.isPaid
        spend.isRecurring = request.isRecurring
        spend.status = SpendStatus.valueOf(request.status)
        spendRepository.save(spend)
    }

    fun deleteSpend(id: Long) {
        logger.info("m=deleteSpend - id: $id")
        val spend = spendRepository.findById(id).orElseThrow {
            IllegalArgumentException("Spend with id $id not found")
        }
        spendRepository.delete(spend)
    }

    companion object {
        fun isDue(dueDate: String): Boolean {
            return LocalDate.parse(dueDate).isBefore(LocalDate.now())
        }
    }

}
