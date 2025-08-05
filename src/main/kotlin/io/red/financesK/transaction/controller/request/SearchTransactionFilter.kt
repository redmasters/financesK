package io.red.financesK.transaction.controller.request

import java.math.BigDecimal
import java.time.LocalDate
import io.red.financesK.transaction.model.InstallmentInfo

/**
 * DTO para filtros de busca de transações. Todos os campos são opcionais.
 */
data class SearchTransactionFilter(
    val description: String? = null,
    val amount: BigDecimal? = null,
    val type: String? = null,
    val categoryId: Int? = null,
    val startDate: LocalDate? = null,
    val notes: String? = null,
    val recurrencePattern: String? = null,
    val totalInstallments: Int? = null,
    val userId: Int? = null,
    val installmentInfo: InstallmentInfo? = null
)

