package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import com.karlvcrisostomo.financialmatrix.core.data.UserPreferences
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity

enum class TransactionSortOrder(val displayName: String) {
    LATEST(displayName = "Latest"),
    HIGHEST_AMOUNT(displayName = "Highest ₱"),
    LOWEST_AMOUNT(displayName = "Lowest ₱")
}

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
        monthlyBudgetLimit = 5000.0
    ),
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val cashSpent: Double = 0.0,
    val creditSpent: Double = 0.0,
    val netSavings: Double = 0.0,
    val savingsRate: Double = 0.0,
    val categoryAmounts: Map<String, Double> = emptyMap(),
    val availableCategories: List<String> = emptyList(),
    val errorMessage: String? = null
)