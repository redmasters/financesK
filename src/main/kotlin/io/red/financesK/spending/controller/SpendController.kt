package io.red.financesK.spending.controller

import io.red.financesK.spending.controller.request.CreateSpendRequest
import io.red.financesK.spending.controller.request.EditSpendRequest
import io.red.financesK.spending.controller.response.SpendResponse
import io.red.financesK.spending.enums.SpendStatus
import io.red.financesK.spending.service.SpendService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("/api/spend")
class SpendController(
    private val spendService: SpendService
) {
    @PostMapping
    fun createSpend(@RequestBody request: CreateSpendRequest): ResponseEntity<Long?> {
        val spend = spendService.createSpend(request)
        return ResponseEntity
            .created(URI("/api/spend/${spend.id}"))
            .body(spend.id)
    }

    @GetMapping("/all")
    fun getAllSpends(): List<SpendResponse> {
        return spendService.getAllSpends()
    }

    @GetMapping("/{id}")
    fun getSpendById(@PathVariable id: Long): ResponseEntity<SpendResponse> {
        val spend = spendService.getSpendById(id)
        return ResponseEntity.ok(spend)
    }

    @GetMapping("/category/{categoryId}")
    fun getSpendByCategoryId(@PathVariable categoryId: Long): ResponseEntity<List<SpendResponse>> {
        val spends = spendService.getSpendByCategoryId(categoryId)
        return ResponseEntity.ok(spends)
    }

    @GetMapping("status/all")
    fun getAllSpendStatus(): ResponseEntity<List<SpendStatus>> {
        val status = spendService.getAllSpendStatus()
        return ResponseEntity.ok(status)
    }

    @GetMapping("/status/id/{id}")
    fun getSpendStatusById(@PathVariable id: Long): ResponseEntity<SpendStatus> {
        val status = spendService.getSpendStatusById(id)
        return ResponseEntity.ok(status)
    }

    @GetMapping("/status")
    fun getSpendStatusByName(@RequestParam name: String): ResponseEntity<List<SpendResponse>?> {
        val status = spendService.getSpendByStatusByName(name)
        return ResponseEntity.ok(status)
    }

    @PutMapping("/{id}")
    fun editSpend(@PathVariable id: Long, @RequestBody request: EditSpendRequest): ResponseEntity<Void> {
        spendService.editSpend(id, request)
        return ResponseEntity.status(204).build()
    }

    @DeleteMapping("/{id}")
    fun deleteSpend(@PathVariable id: Long): ResponseEntity<Void> {
        spendService.deleteSpend(id)
        return ResponseEntity.noContent().build()
    }


}
