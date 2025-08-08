package io.red.financesK.dashboard.controller

import io.red.financesK.dashboard.controller.response.MonthlyBalance
import io.red.financesK.dashboard.service.BalanceOverviewService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/dashboard")
class DashboardController(
    private val balanceOverviewService: BalanceOverviewService
) {
    @GetMapping("/balance-overview")
    fun getBalanceOverview(
        @RequestParam accountIds: List<Int>,
        @RequestParam startDate: String,
        @RequestParam endDate: String,
        @RequestParam userId: Int
    ): ResponseEntity<MonthlyBalance> {
        balanceOverviewService.getMonthlyBalanceOverview(accountIds, startDate, endDate, userId)
        return ResponseEntity.ok(
            balanceOverviewService.getMonthlyBalanceOverview(accountIds, startDate, endDate, userId)
        )
    }
}
