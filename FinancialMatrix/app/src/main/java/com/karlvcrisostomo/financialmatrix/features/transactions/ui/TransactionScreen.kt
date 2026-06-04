package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.karlvcrisostomo.financialmatrix.R
import com.karlvcrisostomo.financialmatrix.core.util.toCsvString
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardEntity
import com.karlvcrisostomo.financialmatrix.features.creditcards.ui.CreditCardDialog
import com.karlvcrisostomo.financialmatrix.features.creditcards.ui.CreditCardScreen
import com.karlvcrisostomo.financialmatrix.features.creditcards.ui.CreditCardViewModel
import com.karlvcrisostomo.financialmatrix.features.income.ui.AddIncomeDialog
import com.karlvcrisostomo.financialmatrix.features.income.ui.IncomeScreen
import com.karlvcrisostomo.financialmatrix.ui.theme.LightGold
import com.karlvcrisostomo.financialmatrix.ui.theme.MidnightNavy
import com.karlvcrisostomo.financialmatrix.ui.theme.PremiumGold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String) {
    object Expenses : Screen("expenses", "Expenses Ledger")
    object Income : Screen("income", "Income Ledger")
    object CreditCards : Screen("credit_cards", "Credit Cards")
    object Settings : Screen("settings", "Settings")
    object Loading : Screen("loading", "Initializing...")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel,
    ccViewModel: CreditCardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val ccUiState by ccViewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showAddDialog = remember { mutableStateOf(false) }
    val showAddIncomeDialog = remember { mutableStateOf(false) }
    val showAddCardDialog = remember { mutableStateOf(false) }
    val showRecurringDialog = remember { mutableStateOf(false) }
    val editingCard = remember { mutableStateOf<CreditCardEntity?>(null) }
    val showBudgetDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            scope.launch(Dispatchers.IO) { // MANDATE: Dispatch I/O off the main thread
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        val csvData = uiState.transactions.toCsvString()
                        outputStream.write(csvData.toByteArray())
                        Log.d("Export", "CSV Exported successfully to $it")
                        snackbarHostState.showSnackbar("Ledger exported successfully")
                    }
                } catch (e: Exception) {
                    Log.e("Export", "Failed to export CSV", e)
                    snackbarHostState.showSnackbar("Failed to export ledger: ${e.message}")
                }
            }
        }
    }

    val currencySymbol = uiState.userPreferences.currencySymbol

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet {
                        Spacer(modifier = Modifier.height(12.dp))
                        NavigationDrawerItem(
                            label = { Text(Screen.Expenses.title) },
                            selected = currentRoute == Screen.Expenses.route,
                            onClick = {
                                navController.navigate(Screen.Expenses.route) {
                                    popUpTo(Screen.Expenses.route) { inclusive = true }
                                }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text(Screen.Income.title) },
                            selected = currentRoute == Screen.Income.route,
                            onClick = {
                                navController.navigate(Screen.Income.route)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text(Screen.CreditCards.title) },
                            selected = currentRoute == Screen.CreditCards.route,
                            onClick = {
                                navController.navigate(Screen.CreditCards.route)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text(Screen.Settings.title) },
                            selected = currentRoute == Screen.Settings.route,
                            onClick = {
                                navController.navigate(Screen.Settings.route)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 28.dp))
                        NavigationDrawerItem(
                            label = { Text("Export CSV") },
                            selected = false,
                            onClick = {
                                exportLauncher.launch("ledger.csv")
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            },
            gesturesEnabled = true
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        if (currentRoute != Screen.Loading.route) {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = when (currentRoute) {
                                            Screen.Expenses.route -> Screen.Expenses.title
                                            Screen.Income.route -> Screen.Income.title
                                            Screen.CreditCards.route -> Screen.CreditCards.title
                                            Screen.Settings.route -> Screen.Settings.title
                                            else -> "Financial Matrix"
                                        }
                                    )
                                },
                                actions = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                                    }
                                }
                            )
                        }
                    },
                    floatingActionButton = {
                        when (currentRoute) {
                            Screen.Expenses.route -> {
                                FloatingActionButton(onClick = { showAddDialog.value = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                                }
                            }
                            Screen.Income.route -> {
                                FloatingActionButton(onClick = { showAddIncomeDialog.value = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Income")
                                }
                            }
                            Screen.CreditCards.route -> {
                                FloatingActionButton(onClick = { showAddCardDialog.value = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Card")
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Loading.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Loading.route) {
                            LoadingScreen(onLoadingFinished = {
                                navController.navigate(Screen.Expenses.route) {
                                    popUpTo(Screen.Loading.route) { inclusive = true }
                                }
                            })
                        }
                        composable(Screen.Expenses.route) {
                            ExpensesScreen(
                                uiState = uiState,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onCategoryFilterChange = { viewModel.updateCategoryFilter(it) },
                                onSortOrderChange = { viewModel.updateSortOrder(it) },
                                onDeleteTransaction = { viewModel.deleteTransaction(it) }
                            )
                        }
                        composable(Screen.Income.route) {
                            IncomeScreen(viewModel = viewModel)
                        }
                        composable(Screen.CreditCards.route) {
                            CreditCardScreen(
                                viewModel = ccViewModel,
                                transactionViewModel = viewModel,
                                onAddCardClick = { showAddCardDialog.value = true },
                                onEditCardClick = { editingCard.value = it }
                            )
                        }
                        composable(Screen.Settings.route) {
                            // Simple Settings UI
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Default Payment Method", style = MaterialTheme.typography.titleMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Default to Credit Card")
                                    Switch(
                                        checked = uiState.userPreferences.defaultIsCreditCard,
                                        onCheckedChange = { viewModel.toggleDefaultPaymentMethod() }
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Monthly Budget Limit", style = MaterialTheme.typography.titleMedium)
                                Button(onClick = { showBudgetDialog.value = true }) {
                                    Text("Set Budget (Current: $currencySymbol${uiState.userPreferences.monthlyBudgetLimit})")
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Ledger Automation", style = MaterialTheme.typography.titleMedium)
                                Button(
                                    onClick = { showRecurringDialog.value = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("Manage Recurring Transactions")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRecurringDialog.value) {
        RecurringManagementDialog(
            viewModel = viewModel,
            ccViewModel = ccViewModel,
            onDismiss = { showRecurringDialog.value = false }
        )
    }

    if (showAddDialog.value) {
        AddTransactionDialog(
            initialIsCreditCard = uiState.userPreferences.defaultIsCreditCard,
            currencySymbol = currencySymbol,
            availableCards = ccUiState.cards.map { it.card.name },
            onDismiss = { showAddDialog.value = false },
            onSave = { newTransaction ->
                viewModel.addTransaction(newTransaction)
                showAddDialog.value = false
            }
        )
    }

    if (showAddIncomeDialog.value) {
        AddIncomeDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showAddIncomeDialog.value = false },
            onSave = { newIncome ->
                viewModel.addIncome(newIncome)
                showAddIncomeDialog.value = false
            }
        )
    }

    if (showAddCardDialog.value) {
        CreditCardDialog(
            onDismiss = { showAddCardDialog.value = false },
            onSave = { newCard ->
                ccViewModel.addCard(newCard)
                showAddCardDialog.value = false
            }
        )
    }

    if (editingCard.value != null) {
        CreditCardDialog(
            card = editingCard.value,
            onDismiss = { editingCard.value = null },
            onSave = { updatedCard ->
                ccViewModel.addCard(updatedCard)
                editingCard.value = null
            }
        )
    }

    if (showBudgetDialog.value) {
        var budgetText by remember { mutableStateOf(uiState.userPreferences.monthlyBudgetLimit.toString()) }
        AlertDialog(
            onDismissRequest = { showBudgetDialog.value = false },
            title = { Text("Set Monthly Budget") },
            text = {
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text("Budget Amount ($currencySymbol)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newLimit = budgetText.toDoubleOrNull() ?: 0.0
                        viewModel.updateMonthlyBudgetLimit(newLimit)
                        showBudgetDialog.value = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LoadingScreen(onLoadingFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500) // Brief transition delay
        onLoadingFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.size(140.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Financial Matrix Ledger",
                color = PremiumGold,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            CircularProgressIndicator(
                color = LightGold,
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
        }
    }
}
