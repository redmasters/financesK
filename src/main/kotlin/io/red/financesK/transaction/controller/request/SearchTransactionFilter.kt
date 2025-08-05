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
    val downPayment: BigDecimal? = null,
    val type: String? = null,
    val categoryId: Int? = null,
    val status: String? = null, // 'PENDING', 'PAID', 'OVERDUE'
    val startDate: LocalDate? = null, // Data inicial para busca por período
    val endDate: LocalDate? = null, // Data final para busca por período
    val dueDate: LocalDate? = null, // Data específica de vencimento
    val notes: String? = null,
    val recurrencePattern: String? = null,
    val totalInstallments: Int? = null,
    val userId: Int? = null,
    // Filtros avançados
    val hasDownPayment: Boolean? = null, // true = apenas com entrada, false = apenas sem entrada
    val isInstallment: Boolean? = null, // true = apenas parceladas, false = apenas à vista
    val categoryName: String? = null, // Busca por nome da categoria
    val minAmount: BigDecimal? = null, // Valor mínimo
    val maxAmount: BigDecimal? = null, // Valor máximo
    val currentMonth: Boolean? = null, // true = apenas do mês atual
    val currentWeek: Boolean? = null, // true = apenas da semana atual
    val currentYear: Boolean? = null // true = apenas do ano atual
)
