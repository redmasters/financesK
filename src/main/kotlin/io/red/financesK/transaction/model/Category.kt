package io.red.financesK.transaction.model

import jakarta.persistence.*

@Entity
@Table(name = "tbl_category")
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,

    @Column(name = "name", nullable = false, unique = true)
    val name: String?,

    @Column(name = "type", nullable = false)
    val type: String?, // 'EXPENSE' or 'INCOME'

    @Column(name = "icon")
    val icon: String? = null,

    @Column(name = "color")
    val color: String? = null,

    @Column(name = "parent_id")
    val parentId: Int? = null
)
