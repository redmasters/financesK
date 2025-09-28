package io.red.financesK.account.balance.enums

enum class AccountOperationType {
    INITIAL_BALANCE,
    SALARY,
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_IN,
    TRANSFER_OUT,
    INTEREST,
    FEE,
    ADJUSTMENT,
    REFUND,
    PAYMENT,
    REWARD,
    LOAN_PAYMENT,
    LOAN_DISBURSEMENT,
    DIVIDEND,
    TAX,
    OTHER;

    companion object {
        fun fromString(value: String): AccountOperationType? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }

    override fun toString(): String {
        return name.lowercase()
    }

    fun isPositive(): Boolean {
        return when (this) {
            DEPOSIT, TRANSFER_IN, INTEREST, REWARD, LOAN_DISBURSEMENT, DIVIDEND, INITIAL_BALANCE, SALARY -> true
            WITHDRAWAL, TRANSFER_OUT, FEE, ADJUSTMENT, REFUND, PAYMENT, LOAN_PAYMENT, TAX, OTHER -> false
        }
    }

    fun isNegative(): Boolean {
        return !isPositive()
    }

    fun isTransfer(): Boolean {
        return this == TRANSFER_IN || this == TRANSFER_OUT
    }
}
