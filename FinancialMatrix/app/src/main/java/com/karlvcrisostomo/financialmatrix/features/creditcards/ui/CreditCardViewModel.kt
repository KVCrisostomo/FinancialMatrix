package com.karlvcrisostomo.financialmatrix.features.creditcards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.karlvcrisostomo.financialmatrix.FinancialMatrixApplication
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardEntity
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import com.karlvcrisostomo.financialmatrix.domain.util.StatementCycleCalculator
import com.karlvcrisostomo.financialmatrix.domain.model.TransactionCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDate

class CreditCardViewModel(
    private val cardRepository: CreditCardRepository,
    private val transactionRepository: TransactionRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {

    val uiState: StateFlow<CreditCardUiState> = combine(
        cardRepository.getAllCards(),
        transactionRepository.getAllTransactions()
    ) { cards, transactions ->
        val today = LocalDate.now(clock)
        
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
        transactions: List<TransactionEntity>,
        today: LocalDate
    ): CreditCardStats {
        val (statementWindowStart, statementWindowEnd) = StatementCycleCalculator.calculatePreviousStatementWindow(
            card.billingDay,
            today
        )

        // Filter transactions for this specific card
        val cardTransactions = transactions.filter { it.isCreditCard && it.accountName == card.name }

        // Statement balance is the sum of transactions in the previous statement window
        val statementBalance = cardTransactions
            .filter { it.date in statementWindowStart..statementWindowEnd }
            .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }

        // Current balance is stored in the entity and updated via atomic payments
        // However, to keep it consistent with existing non-payment transactions, 
        // we use the stored balance which should represent (all expenses - all payments).
        val currentBalance = card.balance
        val remainingLimit = card.creditLimit.subtract(currentBalance)
        val utilization = if (card.creditLimit > BigDecimal.ZERO) {
            currentBalance.divide(card.creditLimit, 4, RoundingMode.HALF_EVEN)
                .multiply(BigDecimal("100"))
        } else {
            BigDecimal.ZERO
        }

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