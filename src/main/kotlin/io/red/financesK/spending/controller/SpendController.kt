package io.red.financesK.spending.controller

import io.red.financesK.spending.controller.request.CreateSpendRequest
import io.red.financesK.spending.controller.response.SpendResponse
import io.red.financesK.spending.service.SpendService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/spend")
class SpendController(
    private val spendService: SpendService
) {
    @PostMapping("/create")
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
}
