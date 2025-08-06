package io.red.financesK.account.balance.model

import io.red.financesK.account.model.Account
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.time.Instant.now

@Entity
@Table(name = "tbl_account_balance_history")
data class AccountBalanceHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_history_id", nullable = false, unique = true)
    val balanceHistoryId: Int? = null,

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false, referencedColumnName = "account_id")
    val account: Account,

    @Column(name = "balance_history", nullable = false, precision = 10, scale = 2)
    val balance: String, // Using String to handle potential formatting issues

    @Column(name = "balance_timestamp", nullable = false)
    val balanceTimestamp: Instant = now()

)
