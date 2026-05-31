package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity

data class TransactionUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionEntity> = emptyList(),
    val errorMessage: String? = null
)