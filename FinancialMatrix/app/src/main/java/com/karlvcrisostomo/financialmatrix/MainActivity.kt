package com.karlvcrisostomo.financialmatrix

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.karlvcrisostomo.financialmatrix.core.security.BiometricAuthenticator
import com.karlvcrisostomo.financialmatrix.features.creditcards.ui.CreditCardViewModel
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.TransactionScreen
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.TransactionViewModel
import com.karlvcrisostomo.financialmatrix.ui.theme.FinancialMatrixTheme
import com.karlvcrisostomo.financialmatrix.ui.theme.MidnightNavy
import com.karlvcrisostomo.financialmatrix.ui.theme.PremiumGold
import com.karlvcrisostomo.financialmatrix.ui.theme.LightGold
import kotlinx.coroutines.delay

class MainActivity : FragmentActivity() {

    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModel.Factory
    }

    private val creditCardViewModel: CreditCardViewModel by viewModels {
        CreditCardViewModel.Factory
    }

    private val biometricAuthenticator by lazy {
        BiometricAuthenticator(this)
    }

    private var isAuthenticated by mutableStateOf(false)
    private var isAuthenticating by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Trigger initial authentication
        attemptAuthentication()

        setContent {
            FinancialMatrixTheme {
                val ccUiState by creditCardViewModel.uiState.collectAsState()
                
                LaunchedEffect(ccUiState) {
                    Log.d("CC_DEBUG", "ccUiState: isLoading=${ccUiState.isLoading}, cardsCount=${ccUiState.cards.size}, error=${ccUiState.errorMessage}")
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MidnightNavy
                ) {
                    if (isAuthenticated) {
                        TransactionScreen(
                            viewModel = transactionViewModel,
                            ccViewModel = creditCardViewModel
                        )
                    } else {
                        UnlockLedgerScreen(
                            isAuthenticating = isAuthenticating,
                            onUnlockClick = { attemptAuthentication() }
                        )
                    }
                }
            }
        }
    }

    private fun attemptAuthentication() {
        if (isAuthenticating) return
        isAuthenticating = true
        biometricAuthenticator.authenticate(
            onSuccess = {
                isAuthenticated = true
                isAuthenticating = false
            },
            onError = { errorCode, errString ->
                isAuthenticating = false
                Toast.makeText(this, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            },
            onFailed = {
                isAuthenticating = false
                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun UnlockLedgerScreen(
    isAuthenticating: Boolean,
    onUnlockClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Financial Matrix Ledger",
            color = PremiumGold,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        if (isAuthenticating) {
            CircularProgressIndicator(color = LightGold)
        } else {
            Button(
                onClick = onUnlockClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumGold,
                    contentColor = MidnightNavy
                ),
                modifier = Modifier.height(56.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Unlock Ledger",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Secure Biometric Entry",
            color = LightGold.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
