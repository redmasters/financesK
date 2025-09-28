package io.red.financesK.account.enums

enum class AccountTypeEnum {
    CONTA_CORRENTE,
    CARTEIRA,
    CARTAO_CREDITO,
    POUPANCA;

    companion object {
        fun fromString(value: String?): AccountTypeEnum? {
            return AccountTypeEnum.entries.find { it.name.equals(value, ignoreCase = true) }
        }

    }

    override fun toString(): String {
        return name.lowercase()
    }

}
