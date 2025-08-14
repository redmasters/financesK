package io.red.financesK.bank.repository

import io.red.financesK.bank.model.BankInstitution
import org.springframework.data.jpa.repository.JpaRepository

interface BankInstitutionRepository : JpaRepository<BankInstitution, Int> {
}
