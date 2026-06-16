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
import com.karlvcrisostomo.financialmatrix.domain.model.TransactionCategory
import com.karlvcrisostomo.financialmatrix.domain.usecase.InvalidFundingSourceException
import com.karlvcrisostomo.financialmatrix.domain.usecase.ValidateTransactionSourceUseCase
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringTransactionEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringTransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val incomeRepository: IncomeRepository,
    private val recurringRepository: RecurringTransactionRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val workManager: WorkManager,
    private val validateSourceUseCase: ValidateTransactionSourceUseCase = ValidateTransactionSourceUseCase(),
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(TransactionSortOrder.LATEST)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _errorMessage = MutableStateFlow<String?>(null)
    
    private val _actionEvents = MutableSharedFlow<TransactionAction>()
    val actionEvents = _actionEvents.asSharedFlow()

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
        _errorMessage,
        preferencesRepository.userPreferencesFlow
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val transactionList = flows[0] as List<TransactionEntity>
        @Suppress("UNCHECKED_CAST")
        val incomeList = flows[1] as List<IncomeEntity>
        val sortOrder = flows[2] as TransactionSortOrder
        val category = flows[3] as String?
        val query = flows[4] as String
        val errorMessage = flows[5] as String?
        val preferences = flows[6] as com.karlvcrisostomo.financialmatrix.core.data.UserPreferences

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

        // 3. Compute Analytics
        val today = java.time.LocalDate.now()
        val monthlyTransactions = transactionList.filter { 
            it.date.month == today.month && 
            it.date.year == today.year && 
            !TransactionCategory.from(it.category).isInternalTransfer()
        }
        
        val totalSpent = monthlyTransactions.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
        val cashSpent = monthlyTransactions.filter { !it.isCreditCard }.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
        val creditSpent = monthlyTransactions.filter { it.isCreditCard }.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
        
        val categoryAmounts = monthlyTransactions.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) } }

        // 4. Compute Monthly Income
        val totalIncome = incomeList
            .filter { it.date.month == today.month && it.date.year == today.year }
            .fold(BigDecimal.ZERO) { acc, i -> acc.add(i.amount) }

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
            isLoading = false,
            errorMessage = errorMessage
        )
    }
    .flowOn(defaultDispatcher)
    .catch { exception ->
        emit(TransactionUiState(errorMessage = exception.message, isLoading = false))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
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

    fun clearError() {
        _errorMessage.value = null
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

    fun updateMonthlyBudgetLimit(limit: BigDecimal) {
        viewModelScope.launch {
            preferencesRepository.updateMonthlyBudgetLimit(limit)
            triggerBudgetCheck()
        }
    }

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                // 1. Validate the funding source
                validateSourceUseCase(transaction.category, transaction.isCreditCard)
                
                // 2. Route based on transaction type and payment method
                val category = TransactionCategory.from(transaction.category)
                when {
                    // Credit Card Payment: Decreases CC balance
                    category is TransactionCategory.CreditCardPayment && transaction.targetCreditCardId != null -> {
                        repository.insertCreditCardPayment(transaction, transaction.targetCreditCardId)
                    }
                    // Expense paid with CC: Increases CC balance (liability)
                    transaction.isCreditCard && category !is TransactionCategory.CreditCardPayment && transaction.targetCreditCardId != null -> {
                        repository.insertExpenseWithBalanceUpdate(transaction, transaction.targetCreditCardId)
                    }
                    // Standard expense (Cash/Primary): Just record it
                    else -> {
                        repository.insertTransaction(transaction)
                    }
                }
                
                _errorMessage.value = null
                triggerBudgetCheck()
                _actionEvents.emit(TransactionAction.TransactionSaved)
            } catch (e: InvalidFundingSourceException) {
                _errorMessage.value = e.message
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save transaction: ${e.message}"
            }
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
            val category = TransactionCategory.from(transaction.category)
            if (transaction.isCreditCard || category is TransactionCategory.CreditCardPayment) {
                repository.deleteTransactionWithBalanceUpdate(transaction)
            } else {
                repository.deleteTransaction(transaction)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinancialMatrixApplication
                return TransactionViewModel(
                    application.transactionRepository,
                    application.incomeRepository,
                    application.recurringTransactionRepository,
                    application.userPreferencesRepository,
                    WorkManager.getInstance(application),
                    ValidateTransactionSourceUseCase(),
                    Dispatchers.Default
                ) as T
            }
        }
    }
}