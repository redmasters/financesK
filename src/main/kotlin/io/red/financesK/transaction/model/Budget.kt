package io.red.financesK.transaction.model

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "tbl_budget")
data class Budget(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Int,

    @Column(name = "category_id")
    val categoryId: Int? = null,

    @Column(name = "amount", nullable = false)
    val amount: Int,

    @Column(name = "budget_month", nullable = false)
    val month: LocalDate,

    @Column(name = "created_at")
    val createdAt: Instant? = null
)
