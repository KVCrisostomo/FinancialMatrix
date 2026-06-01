package com.karlvcrisostomo.financialmatrix.features.creditcards.ui

import app.cash.turbine.test
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardEntity
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CreditCardViewModelTest {

    private val cardRepository: CreditCardRepository = mockk(relaxed = true)
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val mockCards = listOf(
        CreditCardEntity(1, "Visa Gold", 15, 5, 50000.0)
    )

    // Setup transactions for June 1st calculation
    private val mockTransactions = listOf(
        // In June billing window (May 16 - June 15)
        TransactionEntity(1, "Store A", 1000.0, LocalDate.of(2026, 6, 1), "Other", true, "Visa Gold"),
        // In previous window (April 16 - May 15)
        TransactionEntity(2, "Store B", 2000.0, LocalDate.of(2026, 5, 1), "Other", true, "Visa Gold"),
        // Not a credit card
        TransactionEntity(3, "Store C", 500.0, LocalDate.of(2026, 6, 1), "Other", false, "Visa Gold"),
        // Different account
        TransactionEntity(4, "Store D", 300.0, LocalDate.of(2026, 6, 1), "Other", true, "Mastercard")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { cardRepository.getAllCards() } returns flowOf(mockCards)
        every { transactionRepository.getAllTransactions() } returns flowOf(mockTransactions)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState computes correct stats for card`() = runTest {
        val viewModel = CreditCardViewModel(cardRepository, transactionRepository)

        viewModel.uiState.test {
            val firstEmission = awaitItem()
            val state = if (firstEmission.isLoading) awaitItem() else firstEmission
            
            assertEquals(1, state.cards.size)
            val stats = state.cards[0]
            
            assertEquals("Visa Gold", stats.card.name)
            // Total current balance for Visa Gold (1000 + 2000 = 3000)
            assertEquals(3000.0, stats.currentBalance, 0.0)
            // Statement balance for last period (April 16 - May 15) contains 2000.0
            // Wait, logic in VM uses today. 
            // If today is June 1, latest billing date is May 15.
            // Window start is April 16. End is May 15.
            // Transaction at 2026-05-01 is in window.
            assertEquals(2000.0, stats.statementBalance, 0.0)
            assertEquals(47000.0, stats.remainingLimit, 0.0)
            assertTrue(stats.utilizationPercentage > 0)
        }
    }

    @Test
    fun `addCard calls repository`() = runTest {
        val viewModel = CreditCardViewModel(cardRepository, transactionRepository)
        val newCard = CreditCardEntity(0, "New Card", 1, 10, 10000.0)

        viewModel.addCard(newCard)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { cardRepository.insertCard(newCard) }
    }

    @Test
    fun `deleteCard calls repository`() = runTest {
        val viewModel = CreditCardViewModel(cardRepository, transactionRepository)
        val cardToDelete = mockCards[0]

        viewModel.deleteCard(cardToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { cardRepository.deleteCard(cardToDelete) }
    }
}
