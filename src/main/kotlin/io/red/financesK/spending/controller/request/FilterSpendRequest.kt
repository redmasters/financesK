package io.red.financesK.spending.controller.request

data class FilterSpendRequest(
    val startDate: String? = null,
    val endDate: String? = null,
    val isPaid: Boolean? = null,
    val isDue: Boolean? = null,
    val categoryId: Long? = null

) {
    fun toQueryParams(): Map<String, Any> {
        return mapOf(
            "startDate" to (startDate ?: ""),
            "endDate" to (endDate ?: ""),
            "isPaid" to (isPaid ?: false),
            "isDue" to (isDue ?: false),
            "categoryId" to (categoryId ?: 0L)
        )
    }
}

