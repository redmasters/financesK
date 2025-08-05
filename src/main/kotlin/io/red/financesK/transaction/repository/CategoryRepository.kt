package io.red.financesK.transaction.repository

import io.red.financesK.transaction.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Int>

