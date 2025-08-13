package io.red.financesK.category.repository

import io.red.financesK.category.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Int> {

    fun findByName(name: String): Category?

    fun findByNameContainingIgnoreCase(name: String): List<Category>

    fun findByParentIdIsNull(): List<Category>

    fun findCategoriesByParentId(parentId: Int): List<Category>

    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL ORDER BY c.name")
    fun findRootCategories(): List<Category>

    @Query("SELECT c FROM Category c WHERE c.parentId= :parentId ORDER BY c.name")
    fun findSubcategoriesByParentId(@Param("parentId") parentId: Int): List<Category>
}
