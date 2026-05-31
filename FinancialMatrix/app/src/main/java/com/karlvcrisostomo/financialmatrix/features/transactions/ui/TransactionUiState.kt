package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import com.karlvcrisostomo.financialmatrix.core.data.UserPreferences
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity

enum class TransactionSortOrder(val displayName: String) {
    LATEST(displayName = "Latest"),
    HIGHEST_AMOUNT(displayName = "Highest ₱"),
    LOWEST_AMOUNT(displayName = "Lowest ₱")
}

data class TransactionUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionEntity> = emptyList(),
    val sortOrder: TransactionSortOrder = TransactionSortOrder.LATEST,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val userPreferences: UserPreferences = UserPreferences(
        currencySymbol = "₱",
        defaultIsCreditCard = false,
        monthlyBudgetLimit = 5000.0
    ),
    val availableCategories: List<String> = emptyList(),
    val errorMessage: String? = null
)