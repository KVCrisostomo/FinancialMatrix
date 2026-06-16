package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import com.karlvcrisostomo.financialmatrix.core.data.UserPreferences
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity

enum class TransactionSortOrder(val displayName: String) {
    LATEST(displayName = "Latest"),
    HIGHEST_AMOUNT(displayName = "Highest ₱"),
    LOWEST_AMOUNT(displayName = "Lowest ₱")
}

data class SavingsUiState(
    val netSavings: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    val savingsRate: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    val isLoading: Boolean = false
)

data class TransactionUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionEntity> = emptyList(),
    val incomeTransactions: List<IncomeEntity> = emptyList(),
    val sortOrder: TransactionSortOrder = TransactionSortOrder.LATEST,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val userPreferences: UserPreferences = UserPreferences(
        currencySymbol = "₱",
        defaultIsCreditCard = false,
        monthlyBudgetLimit = java.math.BigDecimal("5000.00")
    ),
    val totalSpent: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    val totalIncome: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    val cashSpent: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    val creditSpent: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    val categoryAmounts: Map<String, java.math.BigDecimal> = emptyMap(),
    val availableCategories: List<String> = emptyList(),
    val errorMessage: String? = null
)

sealed class TransactionAction {
    data object TransactionSaved : TransactionAction()
}