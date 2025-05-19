package io.red.financesK.spending.enums

enum class SpendStatus (
    val status: String,
){
    PENDING("PENDING"),
    PAID("PAID"),
    OVERDUE("OVERDUE"),
    RECURRING("RECURRING"),
    DUE("DUE"),
    UNPAID("UNPAID"),
    ;

}
