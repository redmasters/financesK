package io.red.financesK.account.model

import io.red.financesK.account.enums.AccountTypeEnum
import io.red.financesK.bank.model.BankInstitution
import io.red.financesK.user.model.AppUser
import jakarta.persistence.*
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_institution_id", nullable = true)
    var bankInstitution: BankInstitution? = null,

    @Column(name = "account_type", nullable = true, length = 50)
    @Enumerated(EnumType.STRING)
    var accountType: AccountTypeEnum? = null,

    @Column(name = "account_credit_limit", nullable = true)
    var accountCreditLimit: Int? = null,

    @Column(name = "account_statement_closing_date", nullable = true)
    var accountStatementClosingDate: Int? = null,

    @Column(name = "account_payment_due_date", nullable = true)
    var accountPaymentDueDate: Int? = null,

    @Column(name = "account_current_balance", nullable = true)
    var accountCurrentBalance: Int? = null,

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
