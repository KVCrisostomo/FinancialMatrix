package com.karlvcrisostomo.financialmatrix.features.analytics.data

import java.math.BigDecimal

data class CategoryAggregation(
    val interval: String,
    val category: String,
    val totalAmount: BigDecimal
)
