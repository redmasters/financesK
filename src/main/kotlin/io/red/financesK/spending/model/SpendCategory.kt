package io.red.financesK.spending.model

import jakarta.persistence.*

@Entity
@Table(name = "finances_spend_category")
data class SpendCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    val id: Long? = null,
    val name: String,
    val description: String? = null
)
