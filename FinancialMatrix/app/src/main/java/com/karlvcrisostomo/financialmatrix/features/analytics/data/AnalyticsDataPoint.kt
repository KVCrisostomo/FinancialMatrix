package com.karlvcrisostomo.financialmatrix.features.analytics.data

import java.math.BigDecimal

data class AnalyticsDataPoint(
    val interval: String,
    val totalExpenses: BigDecimal,
    val totalIncome: BigDecimal
)
