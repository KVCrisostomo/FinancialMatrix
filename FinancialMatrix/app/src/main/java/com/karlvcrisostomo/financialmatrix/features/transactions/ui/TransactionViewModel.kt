package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.karlvcrisostomo.financialmatrix.FinancialMatrixApplication
import com.karlvcrisostomo.financialmatrix.core.data.UserPreferencesRepository
import com.karlvcrisostomo.financialmatrix.core.worker.BudgetMonitorWorker
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringTransactionEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringTransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository
import com.karlvcrisostomo.financialmatrix.domain.model.TransactionCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val incomeRepository: IncomeRepository,
    private val recurringRepository: RecurringTransactionRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(TransactionSortOrder.LATEST)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    
    private val standardCategories = TransactionCategory.getAllStandard().map { it.displayName }

    val recurringTransactions: StateFlow<List<RecurringTransactionEntity>> = 
        recurringRepository.getAllRecurringTransactions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transform the raw database Flow into a cold state stream for the UI
    val uiState: StateFlow<TransactionUiState> = combine(
        repository.getAllTransactions(),
        incomeRepository.getAllIncome(),
        _sortOrder,
        _selectedCategory,
        _searchQuery,
        preferencesRepository.userPreferencesFlow
    ) { flows ->
        val transactionList = flows[0] as List<TransactionEntity>
        val incomeList = flows[1] as List<IncomeEntity>
        val sortOrder = flows[2] as TransactionSortOrder
        val category = flows[3] as String?
        val query = flows[4] as String
        val preferences = flows[5] as com.karlvcrisostomo.financialmatrix.core.data.UserPreferences

        // 1. Apply Filtering
        val filteredList = transactionList
            .filter { transaction ->
                val matchesCategory = category == null || transaction.category == category
                val matchesQuery = query.isBlank() || 
                    transaction.description.contains(query, ignoreCase = true) ||
                    transaction.category.contains(query, ignoreCase = true)
                matchesCategory && matchesQuery
            }

        // 2. Apply Sorting
        val sortedList = when (sortOrder) {
            TransactionSortOrder.LATEST -> filteredList.sortedByDescending { it.id }
            TransactionSortOrder.HIGHEST_AMOUNT -> filteredList.sortedByDescending { it.amount }
            TransactionSortOrder.LOWEST_AMOUNT -> filteredList.sortedBy { it.amount }
        }

        // 3. Compute Analytics (Excluding internal transfers like CC Payment from spending totals)
        val today = java.time.LocalDate.now()
        val monthlyTransactions = transactionList.filter { 
            it.date.month == today.month && 
            it.date.year == today.year && 
            !TransactionCategory.from(it.category).isInternalTransfer()
        }
        
        val totalSpent = monthlyTransactions.sumOf { it.amount }
        val cashSpent = monthlyTransactions.filter { !it.isCreditCard }.sumOf { it.amount }
        val creditSpent = monthlyTransactions.filter { it.isCreditCard }.sumOf { it.amount }
        
        val categoryAmounts = monthlyTransactions.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }

        // 4. Compute Monthly Income
        val totalIncome = incomeList
            .filter { it.date.month == today.month && it.date.year == today.year }
            .sumOf { it.amount }

        // 5. Compute Savings KPIs
        val netSavings = totalIncome - totalSpent
        val savingsRate = if (totalIncome > 0) (netSavings / totalIncome) * 100 else 0.0

        TransactionUiState(
            transactions = sortedList,
            incomeTransactions = incomeList,
            sortOrder = sortOrder,
            selectedCategory = category,
            searchQuery = query,
            userPreferences = preferences,
            totalSpent = totalSpent,
            totalIncome = totalIncome,
            cashSpent = cashSpent,
            creditSpent = creditSpent,
            netSavings = netSavings,
            savingsRate = savingsRate,
            categoryAmounts = categoryAmounts,
            availableCategories = standardCategories,
            isLoading = false
        )
    }
    .catch { exception ->
        emit(TransactionUiState(errorMessage = exception.message, isLoading = false))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Grace period for configuration changes (like screen rotations)
        initialValue = TransactionUiState(isLoading = true)
    )


    fun updateSortOrder(order: TransactionSortOrder) {
        _sortOrder.value = order
    }

    fun updateCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleDefaultPaymentMethod() {
        viewModelScope.launch {
            val current = uiState.value.userPreferences.defaultIsCreditCard
            preferencesRepository.updateDefaultIsCreditCard(!current)
        }
    }

    fun updateCurrencySymbol(symbol: String) {
        viewModelScope.launch {
            preferencesRepository.updateCurrencySymbol(symbol)
        }
    }

    fun updateMonthlyBudgetLimit(limit: Double) {
        viewModelScope.launch {
            preferencesRepository.updateMonthlyBudgetLimit(limit)
            triggerBudgetCheck()
        }
    }

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
            triggerBudgetCheck()
        }
    }

    fun addIncome(income: IncomeEntity) {
        viewModelScope.launch {
            incomeRepository.insertIncome(income)
        }
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch {
            incomeRepository.deleteIncome(income)
        }
    }

    fun addRecurringTransaction(recurring: RecurringTransactionEntity) {
        viewModelScope.launch {
            recurringRepository.insertRecurringTransaction(recurring)
        }
    }

    fun deleteRecurringTransaction(recurring: RecurringTransactionEntity) {
        viewModelScope.launch {
            recurringRepository.deleteRecurringTransaction(recurring)
        }
    }

    private fun triggerBudgetCheck() {
        val workRequest = OneTimeWorkRequestBuilder<BudgetMonitorWorker>().build()
        workManager.enqueue(workRequest)
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Factory companion object to instantiate the ViewModel with its required dependencies
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                // Fetch the Application instance from CreationExtras
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinancialMatrixApplication
                return TransactionViewModel(
                    application.transactionRepository,
                    application.incomeRepository,
                    application.recurringTransactionRepository,
                    application.userPreferencesRepository,
                    WorkManager.getInstance(application)
                ) as T
            }
        }
    }
}