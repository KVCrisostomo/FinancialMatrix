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
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class CreditCardViewModelTest {

    private val cardRepository: CreditCardRepository = mockk(relaxed = true)
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    
    // Fixed clock for consistent date calculations
    private val fixedClock = Clock.fixed(
        Instant.parse("2026-06-01T10:00:00Z"),
        ZoneId.of("UTC")
    )

    private val mockCards = listOf(
        CreditCardEntity(1, "Visa Gold", 15, 20, BigDecimal("50000.0"), BigDecimal("3000.0"))
    )

    // Setup transactions for June 1st calculation
    private val mockTransactions = listOf(
        // In June billing window (May 16 - June 15)
        TransactionEntity(1, "Store A", BigDecimal("1000.0"), LocalDate.of(2026, 6, 1), "Other", true, "Visa Gold"),
        // In previous window (April 16 - May 15)
        TransactionEntity(2, "Store B", BigDecimal("2000.0"), LocalDate.of(2026, 5, 1), "Other", true, "Visa Gold"),
        // Not a credit card
        TransactionEntity(3, "Store C", BigDecimal("500.0"), LocalDate.of(2026, 6, 1), "Other", false, "Visa Gold"),
        // Different account
        TransactionEntity(4, "Store D", BigDecimal("300.0"), LocalDate.of(2026, 6, 1), "Other", true, "Mastercard")
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
        val viewModel = CreditCardViewModel(cardRepository, transactionRepository, fixedClock)

        viewModel.uiState.test {
            val firstEmission = awaitItem()
            val state = if (firstEmission.isLoading) awaitItem() else firstEmission
            
            assertEquals(1, state.cards.size)
            val stats = state.cards[0]
            
            assertEquals("Visa Gold", stats.card.name)
            // Total current balance for Visa Gold (stored in card entity)
            assertEquals(BigDecimal("3000.0"), stats.currentBalance)
            // Statement balance for last period (April 16 - May 15) contains 2000.0
            assertEquals(BigDecimal("2000.0"), stats.statementBalance)
            assertEquals(BigDecimal("47000.0"), stats.remainingLimit)
            assertTrue(stats.utilizationPercentage > BigDecimal.ZERO)
        }
    }

    @Test
    fun `addCard calls repository`() = runTest {
        val viewModel = CreditCardViewModel(cardRepository, transactionRepository, fixedClock)
        val newCard = CreditCardEntity(0, "New Card", 1, 20, BigDecimal("10000.0"), BigDecimal.ZERO)

        viewModel.addCard(newCard)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { cardRepository.insertCard(newCard) }
    }

    @Test
    fun `deleteCard calls repository`() = runTest {
        val viewModel = CreditCardViewModel(cardRepository, transactionRepository, fixedClock)
        val cardToDelete = mockCards[0]

        viewModel.deleteCard(cardToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { cardRepository.deleteCard(cardToDelete) }
    }
}
