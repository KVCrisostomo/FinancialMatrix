package com.karlvcrisostomo.financialmatrix.features.creditcards.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.TransactionViewModel

@Composable
fun CreditCardScreen(
    viewModel: CreditCardViewModel,
    transactionViewModel: TransactionViewModel,
    onAddCardClick: () -> Unit,
    onEditCardClick: (CreditCardEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val transUiState by transactionViewModel.uiState.collectAsState()
    val currencySymbol = transUiState.userPreferences.currencySymbol

    Column(modifier = modifier.fillMaxSize()) {
        CreditCardDashboard(
            uiState = uiState,
            currencySymbol = currencySymbol,
            onAddCardClick = onAddCardClick,
            onEditCardClick = onEditCardClick,
            onDeleteCardClick = { viewModel.deleteCard(it) },
            modifier = Modifier.weight(1f)
        )
    }
}
