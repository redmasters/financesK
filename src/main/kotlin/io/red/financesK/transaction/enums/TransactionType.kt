package io.red.financesK.transaction.enums

enum class TransactionType {
    EXPENSE,
    INCOME;

    companion object {
        fun fromString(value: String?): TransactionType? {
            return TransactionType.values().find { it.name.equals(value, ignoreCase = true) }
        }

        fun toString(type: TransactionType?): String? {
            return type?.name?.lowercase()
        }
    }
}
