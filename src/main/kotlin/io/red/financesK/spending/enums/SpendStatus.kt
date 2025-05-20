package io.red.financesK.spending.enums

enum class SpendStatus(
    val id: Long
) {
    PENDING(1L),
    PAID(2L),
    OVERDUE(3L),
    RECURRING(4L),
    DUE(5L),
    UNPAID(6L),

}
