package io.red.financesK.transaction.model

import io.red.financesK.category.model.Category
import io.red.financesK.user.model.AppUser
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: AppUser,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    val category: Category? = null,

    @Column(name = "amount", nullable = false)
    val amount: Int,

    @Column(name = "budget_month", nullable = false)
    val month: LocalDate,

    @Column(name = "created_at")
    val createdAt: Instant? = null
)
