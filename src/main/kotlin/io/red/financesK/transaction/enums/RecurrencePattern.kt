package io.red.financesK.transaction.enums

enum class RecurrencePattern {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    companion object {
        fun fromString(value: String?): RecurrencePattern? {
            return RecurrencePattern.values().find { it.name.equals(value, ignoreCase = true) }
        }

        fun toString(pattern: RecurrencePattern?): String? {
            return pattern?.name?.lowercase()
        }
    }
}
