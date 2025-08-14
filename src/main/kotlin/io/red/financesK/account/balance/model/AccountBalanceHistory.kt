package io.red.financesK.account.balance.model

import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.account.model.Account
import io.red.financesK.transaction.model.Transaction
import jakarta.persistence.*
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

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = true, referencedColumnName = "id")
    val transactionId: Transaction? = null,

    @Column(name = "history_amount", nullable = false)
    val amount: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "history_operation_type", nullable = false, length = 20)
    val operationType: AccountOperationType?,

    @Column(name = "balance_timestamp", nullable = false)
    val balanceTimestamp: Instant = now()

)
