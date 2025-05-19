package io.red.financesK.spending.model

import jakarta.persistence.*

@Entity
@Table(name = "finances_spend_category")
data class SpendCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    val id: Long? = null,
    var name: String,
    var description: String? = null,
    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    var isDeleted: Boolean = false,
)
