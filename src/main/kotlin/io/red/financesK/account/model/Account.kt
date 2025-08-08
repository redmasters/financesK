package io.red.financesK.account.model

import io.red.financesK.user.model.AppUser
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "tbl_account")
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", nullable = false, unique = true)
    val accountId: Int? = null,

    @Column(name = "account_name", nullable = false, length = 100)
    var accountName: String?,

    @Column(name = "account_description", length = 255)
    var accountDescription: String? = null,

    @Column(name = "account_initial_balance", nullable = false, precision = 10, scale = 2)
    var accountInitialBalance: BigDecimal? = null,

    @Column(name = "account_currency", nullable = false, length = 3)
    var accountCurrency: String? = "BRL", // Default currency set to BRL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var userId: AppUser? = null,

    @Column(name = "created_at", nullable = true)
    val createdAt: Instant? = Instant.now(),

    @Column(name = "updated_at", nullable = true)
    var updatedAt: Instant? = null

)
