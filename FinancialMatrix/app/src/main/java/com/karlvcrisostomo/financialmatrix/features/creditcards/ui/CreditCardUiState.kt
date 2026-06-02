package com.karlvcrisostomo.financialmatrix.features.creditcards.ui

import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardEntity
import java.time.LocalDate

data class CreditCardStats(
    val card: CreditCardEntity,
    val statementWindowStart: LocalDate,
    val statementWindowEnd: LocalDate,
    val statementBalance: Double,
    val currentBalance: Double,
    val remainingLimit: Double,
    val utilizationPercentage: Double
)

data class CreditCardUiState(
    val isLoading: Boolean = false,
    val cards: List<CreditCardStats> = emptyList(),
    val errorMessage: String? = null
)
