package io.red.financesK.spending.repository

import io.red.financesK.spending.model.SpendCategory
import org.springframework.data.jpa.repository.JpaRepository

interface SpendCategoryRepository : JpaRepository<SpendCategory, Long> {
    fun findByName(name: String): SpendCategory?
    fun findByIsDeleted(isDeleted: Boolean): List<SpendCategory>
}
