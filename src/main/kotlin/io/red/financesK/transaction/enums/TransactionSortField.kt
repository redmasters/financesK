package io.red.financesK.transaction.enums

enum class TransactionSortField(val fieldName: String) {
    DUE_DATE("dueDate"),
    AMOUNT("amount"),
    TYPE("type"),
    CATEGORY("categoryId"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt")
}

enum class SortDirection {
    ASC,
    DESC
}
