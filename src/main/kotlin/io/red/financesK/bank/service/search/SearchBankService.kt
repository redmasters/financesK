package io.red.financesK.bank.service.search

import io.red.financesK.bank.model.BankInstitution
import io.red.financesK.bank.repository.BankInstitutionRepository
import org.springframework.stereotype.Service

@Service
class SearchBankService(
    private val bankInstitutionRepository: BankInstitutionRepository
) {
    fun findBankById(bankId: Int): BankInstitution? =
        bankInstitutionRepository.findById(bankId).orElseThrow { NoSuchElementException("Bank institution not found") }

    fun getListBanks(): List<BankInstitution> {
        return bankInstitutionRepository.findAll().ifEmpty {
            throw NoSuchElementException("No bank institutions found")
        }
    }
}

