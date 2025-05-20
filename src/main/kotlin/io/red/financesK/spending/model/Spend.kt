package io.red.financesK.spending.model

import io.red.financesK.spending.enums.SpendStatus
import io.red.financesK.spending.enums.SpendStatus.PENDING
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
    var name: String,
    var amount: BigDecimal,
    var dueDate: LocalDate,
    @ManyToOne
    @JoinColumn(name = "category_id")
    var category: SpendCategory,
    var description: String? = null,
    var isDue : Boolean = false,
    var isPaid : Boolean = false,
    var isRecurring : Boolean = false,
    @Enumerated(EnumType.STRING)
    var status : SpendStatus = PENDING
)
