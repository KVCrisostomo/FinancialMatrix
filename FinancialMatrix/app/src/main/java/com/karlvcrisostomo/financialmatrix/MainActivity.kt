package com.karlvcrisostomo.financialmatrix

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.karlvcrisostomo.financialmatrix.core.security.BiometricAuthenticator
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.TransactionScreen
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.TransactionViewModel
import com.karlvcrisostomo.financialmatrix.ui.theme.FinancialMatrixTheme

class MainActivity : FragmentActivity() {

    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModel.Factory
    }

    private val biometricAuthenticator by lazy {
        BiometricAuthenticator(this)
    }

    private var isAuthenticated by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Trigger initial authentication
        attemptAuthentication()

        setContent {
            FinancialMatrixTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAuthenticated) {
                        TransactionScreen(viewModel = transactionViewModel)
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(onClick = { attemptAuthentication() }) {
                                Text("Unlock Ledger")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun attemptAuthentication() {
        biometricAuthenticator.authenticate(
            onSuccess = {
                isAuthenticated = true
            },
            onError = { errorCode, errString ->
                Toast.makeText(this, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            },
            onFailed = {
                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
