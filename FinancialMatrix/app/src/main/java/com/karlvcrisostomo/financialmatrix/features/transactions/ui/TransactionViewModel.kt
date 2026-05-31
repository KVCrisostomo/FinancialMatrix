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
    private val preferencesRepository: UserPreferencesRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(TransactionSortOrder.LATEST)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    
    private val standardCategories = listOf("Food", "Utilities", "Transport", "Entertainment", "Other")

    // Transform the raw database Flow into a cold state stream for the UI
    val uiState: StateFlow<TransactionUiState> = combine(
        repository.getAllTransactions(),
        _sortOrder,
        _selectedCategory,
        _searchQuery,
        preferencesRepository.userPreferencesFlow
    ) { transactionList, sortOrder, category, query, preferences ->
        val filteredList = transactionList
            .filter { transaction ->
                val matchesCategory = category == null || transaction.category == category
                val matchesQuery = query.isBlank() || 
                    transaction.description.contains(query, ignoreCase = true) ||
                    transaction.category.contains(query, ignoreCase = true)
                matchesCategory && matchesQuery
            }

        val sortedList = when (sortOrder) {
            TransactionSortOrder.LATEST -> filteredList.sortedByDescending { it.id }
            TransactionSortOrder.HIGHEST_AMOUNT -> filteredList.sortedByDescending { it.amount }
            TransactionSortOrder.LOWEST_AMOUNT -> filteredList.sortedBy { it.amount }
        }

        TransactionUiState(
            transactions = sortedList,
            sortOrder = sortOrder,
            selectedCategory = category,
            searchQuery = query,
            userPreferences = preferences,
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
                    application.userPreferencesRepository,
                    WorkManager.getInstance(application)
                ) as T
            }
        }
    }
}