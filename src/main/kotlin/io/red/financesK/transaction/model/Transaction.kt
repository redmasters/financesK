package io.red.financesK.transaction.model

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import io.red.financesK.user.model.AppUser
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Instant

@Entity
@Table(name = "tbl_transaction")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,

    @Column(name = "description", nullable = false)
    val description: String,

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    val type: TransactionType? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val categoryId: Category,

    @Column(name = "transaction_date", nullable = false)
    val transactionDate: LocalDate,

    @Column(name = "created_at")
    val createdAt: Instant? = null,

    @Column(name = "notes")
    val notes: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_pattern")
    val recurrencePattern: RecurrencePattern? = null,

    @Type(JsonBinaryType::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "installment_info", columnDefinition = "jsonb")
    val installmentInfo: InstallmentInfo? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val userId: AppUser
) {
    enum class TransactionType {
        EXPENSE,
        INCOME;

        companion object {
            fun fromString(value: String?): TransactionType? {
                return values().find { it.name.equals(value, ignoreCase = true) }
            }

            fun toString(type: TransactionType?): String? {
                return type?.name?.lowercase()
            }
        }
    }

    enum class RecurrencePattern {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY;
        
        companion object {
            fun fromString(value: String?): RecurrencePattern? {
                return values().find { it.name.equals(value, ignoreCase = true) }
            }

            fun toString(pattern: RecurrencePattern?): String? {
                return pattern?.name?.lowercase()
            }
        }
    }
    
}
