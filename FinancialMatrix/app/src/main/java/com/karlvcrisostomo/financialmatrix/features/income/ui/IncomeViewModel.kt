package com.karlvcrisostomo.financialmatrix.features.income.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.karlvcrisostomo.financialmatrix.FinancialMatrixApplication
import com.karlvcrisostomo.financialmatrix.domain.model.TransactionCategory
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.SavingsUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.math.RoundingMode

class IncomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val incomeRepository: IncomeRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    // Decoupled Savings KPI state stream - Reserved for heavy arithmetic on Dispatchers.Default
    val savingsUiState: StateFlow<SavingsUiState> = combine(
        transactionRepository.getAllTransactions(),
        incomeRepository.getAllIncome()
    ) { transactions, income ->
        val today = java.time.LocalDate.now()
        
        val monthlyTransactions = transactions.filter { 
            it.date.month == today.month && 
            it.date.year == today.year && 
            !TransactionCategory.from(it.category).isInternalTransfer()
        }
        val totalSpent = monthlyTransactions.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }

        val monthlyIncome = income.filter { 
            it.date.month == today.month && 
            it.date.year == today.year 
        }
        val totalIncome = monthlyIncome.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.amount) }

        val netSavings = totalIncome.subtract(totalSpent)
        val savingsRate = if (totalIncome > BigDecimal.ZERO) {
            netSavings.divide(totalIncome, 4, RoundingMode.HALF_EVEN).multiply(BigDecimal("100"))
        } else {
            BigDecimal.ZERO
        }

        SavingsUiState(
            netSavings = netSavings.toDouble(),
            savingsRate = savingsRate.toDouble(),
            isLoading = false
        )
    }
    .flowOn(defaultDispatcher) // Strict dispatcher integrity: Default for arithmetic
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SavingsUiState(isLoading = true)
    )

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinancialMatrixApplication
                return IncomeViewModel(
                    application.transactionRepository,
                    application.incomeRepository,
                    Dispatchers.Default
                ) as T
            }
        }
    }
}