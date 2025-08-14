package io.red.financesK.bank.controller

import io.red.financesK.bank.service.search.SearchBankService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/bank")
class BankController(
    private val searchBankService: SearchBankService
) {

    @GetMapping
    fun getBankById(@RequestParam id: Int) = searchBankService.findBankById(id)

    @GetMapping("/all")
    fun getAllBanks() = searchBankService.getListBanks()

}
