package io.red.financesK.spending.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "finances_spend")
data class Spend(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spend_id")
    val id: Long? = null,
    val name: String,
    val amount: BigDecimal,
    val dueDate: LocalDate,
    @ManyToOne
    @JoinColumn(name = "category_id")
    val category: SpendCategory,
    val description: String? = null,
    val isDue : Boolean = false,
    val isPaid : Boolean = false,
    val isRecurring : Boolean = false,
)
