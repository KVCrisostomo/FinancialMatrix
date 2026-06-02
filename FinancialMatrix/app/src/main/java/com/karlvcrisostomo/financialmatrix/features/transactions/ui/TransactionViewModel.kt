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
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val incomeRepository: IncomeRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(TransactionSortOrder.LATEST)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    
    private val standardCategories = listOf("Food", "Utilities", "Transport", "Entertainment", "CC Payment", "Other")

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

        // 3. Compute Analytics (Excluding "CC Payment" from spending totals)
        val activeAnalyticsList = filteredList.filter { it.category != "CC Payment" }
        
        val totalSpent = activeAnalyticsList.sumOf { it.amount }
        val cashSpent = activeAnalyticsList.filter { !it.isCreditCard }.sumOf { it.amount }
        val creditSpent = activeAnalyticsList.filter { it.isCreditCard }.sumOf { it.amount }
        
        val categoryAmounts = activeAnalyticsList.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }

        // 4. Compute Monthly Income
        val today = java.time.LocalDate.now()
        val totalIncome = incomeList
            .filter { it.date.month == today.month && it.date.year == today.year }
            .sumOf { it.amount }

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
                    application.userPreferencesRepository,
                    WorkManager.getInstance(application)
                ) as T
            }
        }
    }
}