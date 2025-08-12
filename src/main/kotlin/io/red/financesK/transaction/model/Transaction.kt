package io.red.financesK.transaction.model

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import io.red.financesK.account.balance.enums.AccountOperationType
import io.red.financesK.account.model.Account
import io.red.financesK.transaction.enums.PaymentStatus
import io.red.financesK.transaction.enums.RecurrencePattern
import io.red.financesK.transaction.enums.TransactionType
import io.red.financesK.user.model.AppUser
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "tbl_transaction")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,

    @Column(name = "description", nullable = false)
    var description: String,

    @Column(name = "amount", nullable = false)
    var amount: Int,

    @Column(name = "down_payment", precision = 10, scale = 2)
    var downPayment: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    var type: TransactionType? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_operation_type", nullable = false, length = 20)
    var operationType: AccountOperationType? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 10)
    var status: PaymentStatus? = PaymentStatus.PENDING,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var categoryId: Category,

    @Column(name = "due_date", nullable = false)
    var dueDate: LocalDate,

    @Column(name = "created_at")
    val createdAt: Instant? = null,

    @Column(name = "updated_at")
    val updatedAt: Instant? = null,

    @Column(name = "notes")
    var notes: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_pattern")
    val recurrencePattern: RecurrencePattern? = null,

    @Type(JsonBinaryType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "installment_info", columnDefinition = "jsonb")
    val installmentInfo: InstallmentInfo? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val userId: AppUser,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    val accountId: Account? = null
)
