package com.karlvcrisostomo.financialmatrix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.TransactionScreen
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.TransactionViewModel
import com.karlvcrisostomo.financialmatrix.ui.theme.FinancialMatrixTheme

class MainActivity : ComponentActivity() {

    // Safely inject our ViewModel using the Application layer repository factory configuration
    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinancialMatrixTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TransactionScreen(viewModel = transactionViewModel)
                }
            }
        }
    }
}