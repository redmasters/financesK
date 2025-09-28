package io.red.financesK.bank.controller

import io.red.financesK.bank.service.search.SearchBankService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/bank")
@CrossOrigin(origins = ["*"], maxAge = 3600)
class BankController(
    private val searchBankService: SearchBankService
) {

    @GetMapping
    fun getBankById(@RequestParam id: Int) = searchBankService.findBankById(id)

    @GetMapping("/all")
    fun getAllBanks() = searchBankService.getListBanks()

}
