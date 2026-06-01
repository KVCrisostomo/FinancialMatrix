package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import app.cash.turbine.test
import com.karlvcrisostomo.financialmatrix.core.data.UserPreferences
import com.karlvcrisostomo.financialmatrix.core.data.UserPreferencesRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository
import androidx.work.WorkRequest
import androidx.work.WorkManager
import androidx.work.Operation
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.TransactionSortOrder
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
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private val repository: TransactionRepository = mockk(relaxed = true)
    private val preferencesRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val mockTransactions = listOf(
        TransactionEntity(1, "Jollibee", 200.0, LocalDate.now(), "Food", true, "Primary"),
        TransactionEntity(2, "Electric Bill", 1500.0, LocalDate.now(), "Utilities", false, "Primary")
    )

    private val mockPreferences = UserPreferences("₱", false, 5000.0)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllTransactions() } returns flowOf(mockTransactions)
        every { preferencesRepository.userPreferencesFlow } returns flowOf(mockPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is correct`() = runTest {
        val viewModel = TransactionViewModel(repository, preferencesRepository, workManager)

        viewModel.uiState.test {
            // StateFlow emits initial value immediately
            val firstEmission = awaitItem()
            // In TransactionViewModel, initialValue is TransactionUiState(isLoading = true)
            // But since flows emit immediately in setup, we might get the loaded state
            if (firstEmission.isLoading) {
                val loadedState = awaitItem()
                assertEquals(mockTransactions.size, loadedState.transactions.size)
            } else {
                assertEquals(mockTransactions.size, firstEmission.transactions.size)
            }
        }
    }

    @Test
    fun `updateSearchQuery filters transactions correctly`() = runTest {
        val viewModel = TransactionViewModel(repository, preferencesRepository, workManager)

        viewModel.uiState.test {
            awaitItem() // Initial load
            
            viewModel.updateSearchQuery("Electric")
            val filteredState = awaitItem()
            
            assertEquals(1, filteredState.transactions.size)
            assertEquals("Electric Bill", filteredState.transactions[0].description)
        }
    }

    @Test
    fun `updateCategoryFilter filters transactions correctly`() = runTest {
        val viewModel = TransactionViewModel(repository, preferencesRepository, workManager)

        viewModel.uiState.test {
            awaitItem() // Initial load
            
            viewModel.updateCategoryFilter("Food")
            val filteredState = awaitItem()
            
            assertEquals(1, filteredState.transactions.size)
            assertEquals("Food", filteredState.transactions[0].category)
        }
    }

    @Test
    fun `updateSortOrder reorders transactions correctly`() = runTest {
        val viewModel = TransactionViewModel(repository, preferencesRepository, workManager)

        viewModel.uiState.test {
            awaitItem() // Initial load
            
            viewModel.updateSortOrder(TransactionSortOrder.HIGHEST_AMOUNT)
            val sortedState = awaitItem()
            
            assertEquals(1500.0, sortedState.transactions[0].amount, 0.0)
            assertEquals(200.0, sortedState.transactions[1].amount, 0.0)
        }
    }

    @Test
    fun `addTransaction calls repository and triggers budget check`() = runTest {
        val viewModel = TransactionViewModel(repository, preferencesRepository, workManager)
        val newTransaction = TransactionEntity(3, "Coffee", 120.0, LocalDate.now(), "Food", false, "Primary")

        viewModel.addTransaction(newTransaction)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.insertTransaction(newTransaction) }
        // Verify workManager interaction
        coVerify { workManager.enqueue(any<WorkRequest>()) }
    }

    @Test
    fun `deleteTransaction calls repository`() = runTest {
        val viewModel = TransactionViewModel(repository, preferencesRepository, workManager)
        val transactionToDelete = mockTransactions[0]

        viewModel.deleteTransaction(transactionToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.deleteTransaction(transactionToDelete) }
    }
}
