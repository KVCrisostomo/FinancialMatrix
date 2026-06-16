package com.karlvcrisostomo.financialmatrix.features.creditcards.ui

import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardEntity
import java.math.BigDecimal
import java.time.LocalDate

data class CreditCardStats(
    val card: CreditCardEntity,
    val statementWindowStart: LocalDate,
    val statementWindowEnd: LocalDate,
    val statementBalance: BigDecimal,
    val currentBalance: BigDecimal,
    val remainingLimit: BigDecimal,
    val utilizationPercentage: BigDecimal
)

data class CreditCardUiState(
    val isLoading: Boolean = false,
    val cards: List<CreditCardStats> = emptyList(),
    val errorMessage: String? = null
)
