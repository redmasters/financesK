package io.red.financesK.spending.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "finances_bank_account")
data class BankAccount (
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "bank_account_id")
    val id: Long? = null,
    var balance: BigDecimal = BigDecimal.ZERO,

)
