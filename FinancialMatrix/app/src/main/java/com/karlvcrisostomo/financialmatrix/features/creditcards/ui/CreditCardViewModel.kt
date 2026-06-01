package com.karlvcrisostomo.financialmatrix.features.creditcards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.karlvcrisostomo.financialmatrix.FinancialMatrixApplication
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardEntity
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class CreditCardViewModel(
    private val cardRepository: CreditCardRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<CreditCardUiState> = combine(
        cardRepository.getAllCards(),
        transactionRepository.getAllTransactions()
    ) { cards, transactions ->
        val today = LocalDate.now()
        
        val stats = cards.map { card ->
            calculateStats(card, transactions, today)
        }

        CreditCardUiState(cards = stats, isLoading = false)
    }
    .catch { e ->
        emit(CreditCardUiState(errorMessage = e.message, isLoading = false))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreditCardUiState(isLoading = true)
    )

    private fun calculateStats(
        card: CreditCardEntity,
        transactions: List<com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity>,
        today: LocalDate
    ): CreditCardStats {
        // Calculate the window for the PREVIOUS statement (the one currently due or recently paid)
        val billingDay = card.billingDay
        
        // Find the most recent billing date that is <= today
        var currentBillingDate = today.withDayOfMonth(
            if (billingDay <= today.lengthOfMonth()) billingDay else today.lengthOfMonth()
        )
        if (currentBillingDate.isAfter(today)) {
            currentBillingDate = currentBillingDate.minusMonths(1)
        }

        val statementWindowEnd = currentBillingDate
        val statementWindowStart = currentBillingDate.minusMonths(1).plusDays(1)

        // Filter transactions for this specific card
        // Note: Currently TransactionEntity has accountName, we might need to match by card name
        val cardTransactions = transactions.filter { it.isCreditCard && it.accountName == card.name }

        val statementBalance = cardTransactions
            .filter { it.date in statementWindowStart..statementWindowEnd }
            .sumOf { it.amount }

        val currentBalance = cardTransactions.sumOf { it.amount }
        val remainingLimit = card.creditLimit - currentBalance
        val utilization = if (card.creditLimit > 0) (currentBalance / card.creditLimit * 100) else 0.0

        return CreditCardStats(
            card = card,
            statementWindowStart = statementWindowStart,
            statementWindowEnd = statementWindowEnd,
            statementBalance = statementBalance,
            currentBalance = currentBalance,
            remainingLimit = remainingLimit,
            utilizationPercentage = utilization
        )
    }

    fun addCard(card: CreditCardEntity) {
        viewModelScope.launch {
            cardRepository.insertCard(card)
        }
    }

    fun deleteCard(card: CreditCardEntity) {
        viewModelScope.launch {
            cardRepository.deleteCard(card)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinancialMatrixApplication
                return CreditCardViewModel(
                    application.creditCardRepository,
                    application.transactionRepository
                ) as T
            }
        }
    }
}
