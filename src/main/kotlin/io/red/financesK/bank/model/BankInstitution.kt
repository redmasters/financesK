package io.red.financesK.bank.model

import jakarta.persistence.*

@Entity
@Table(name = "tbl_bank_institution")
data class BankInstitution(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_institution_id", nullable = false, unique = true)
    val institutionId: Int? = null,

    @Column(name = "bank_institution_name", nullable = false, length = 100)
    var institutionName: String? = null,

    @Column(name = "bank_institution_logo", nullable = true, length = 255)
    var institutionLogo: String? = null
)
