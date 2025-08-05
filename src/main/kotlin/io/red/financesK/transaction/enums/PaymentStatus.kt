package io.red.financesK.transaction.enums

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED;

    companion object {
        fun fromString(value: String?): PaymentStatus? {
            return PaymentStatus.entries.find { it.name.equals(value, ignoreCase = true) }
        }

        fun toString(status: PaymentStatus?): String? {
            return status?.name?.lowercase()
        }
    }
}
