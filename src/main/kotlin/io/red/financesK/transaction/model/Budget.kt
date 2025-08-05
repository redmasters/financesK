package io.red.financesK.transaction.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.Instant
import jakarta.persistence.*

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
    val amount: BigDecimal,

    @Column(name = "budget_month", nullable = false)
    val month: LocalDate,

    @Column(name = "created_at")
    val createdAt: Instant? = null
)
