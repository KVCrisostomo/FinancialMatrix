package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import androidx.work.WorkManager
import androidx.work.WorkRequest
import app.cash.turbine.test
import com.karlvcrisostomo.financialmatrix.core.data.UserPreferences
import com.karlvcrisostomo.financialmatrix.core.data.UserPreferencesRepository
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringTransactionRepository
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
class TransactionViewModelTest {

    private val repository: TransactionRepository = mockk(relaxed = true)
    private val incomeRepository: IncomeRepository = mockk(relaxed = true)
    private val recurringRepo: RecurringTransactionRepository = mockk(relaxed = true)
    private val preferencesRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val mockTransactions = listOf(
        TransactionEntity(1, "Jollibee", 200.0, LocalDate.now(), "Food", true, "Primary"),
        TransactionEntity(2, "Electric Bill", 1500.0, LocalDate.now(), "Utilities", false, "Primary"),
        TransactionEntity(3, "Credit Card Payment", 1000.0, LocalDate.now(), "CC Payment", false, "Primary")
    )

    private val mockIncome = listOf(
        IncomeEntity(1, "Salary", 5000.0, LocalDate.now()),
        IncomeEntity(2, "Freelance", 1200.0, LocalDate.now()),
        // Old income
        IncomeEntity(3, "Old Job", 1000.0, LocalDate.now().minusMonths(2))
    )

    private val mockPreferences = UserPreferences("₱", false, 5000.0)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllTransactions() } returns flowOf(mockTransactions)
        every { incomeRepository.getAllIncome() } returns flowOf(mockIncome)
        every { recurringRepo.getAllRecurringTransactions() } returns flowOf(emptyList())
        every { preferencesRepository.userPreferencesFlow } returns flowOf(mockPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is correct and excludes CC Payment from totals and includes monthly income`() = runTest {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, testDispatcher)

        viewModel.uiState.test {
            // Initial loading state
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            
            // Success state
            val state = awaitItem()
            
            assertEquals(mockTransactions.size, state.transactions.size)
            // 200 (Food) + 1500 (Utilities) = 1700. CC Payment (1000) should be excluded.
            assertEquals(1700.0, state.totalSpent, 0.0)
            // 5000 + 1200 = 6200. Old Job (1000) should be excluded.
            assertEquals(6200.0, state.totalIncome, 0.0)
            assertEquals("₱", state.userPreferences.currencySymbol)
            assertEquals(false, state.userPreferences.defaultIsCreditCard)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial savingsUiState computes Savings KPIs correctly`() = runTest {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, testDispatcher)

        viewModel.savingsUiState.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            
            val state = awaitItem()
            
            // Total Income = 6200.0
            // Total Spent (excluding CC Payment) = 1700.0
            // Net Savings = 6200 - 1700 = 4500.0
            assertEquals(4500.0, state.netSavings, 0.0)
            
            // Savings Rate = (4500 / 6200) * 100 approx 72.58%
            assertEquals(72.58, state.savingsRate, 0.01)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateSearchQuery filters transactions correctly`() = runTest {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, testDispatcher)

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success
            
            viewModel.updateSearchQuery("Electric")
            val filteredState = awaitItem()
            
            assertEquals(1, filteredState.transactions.size)
            assertEquals("Electric Bill", filteredState.transactions[0].description)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateCategoryFilter filters transactions correctly`() = runTest {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, testDispatcher)

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success
            
            viewModel.updateCategoryFilter("Food")
            val filteredState = awaitItem()
            
            assertEquals(1, filteredState.transactions.size)
            assertEquals("Food", filteredState.transactions[0].category)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateSortOrder reorders transactions correctly`() = runTest {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, testDispatcher)

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success
            
            viewModel.updateSortOrder(TransactionSortOrder.HIGHEST_AMOUNT)
            val sortedState = awaitItem()
            
            assertEquals(1500.0, sortedState.transactions[0].amount, 0.0)
            assertEquals(1000.0, sortedState.transactions[1].amount, 0.0)
            assertEquals(200.0, sortedState.transactions[2].amount, 0.0)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTransaction calls repository and triggers budget check`() = runTest {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, testDispatcher)
        val newTransaction = TransactionEntity(3, "Coffee", 120.0, LocalDate.now(), "Food", false, "Primary")

        viewModel.uiState.test {
            awaitItem() // Skip loading
            awaitItem() // Skip initial success

            viewModel.addTransaction(newTransaction)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { repository.insertTransaction(newTransaction) }
            // Verify workManager interaction
            coVerify { workManager.enqueue(any<WorkRequest>()) }
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addIncome calls repository`() = runTest {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, testDispatcher)
        val newIncome = IncomeEntity(4, "Bonus", 500.0, LocalDate.now())

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success
            
            viewModel.addIncome(newIncome)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { incomeRepository.insertIncome(newIncome) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteTransaction calls repository`() = runTest {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, testDispatcher)
        val transactionToDelete = mockTransactions[0]

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success
            
            viewModel.deleteTransaction(transactionToDelete)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { repository.deleteTransaction(transactionToDelete) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `authentication flow transitions correctly from Loading to Success`() = runTest {
        // Mocking is already set up in @Before
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, testDispatcher)
        
        viewModel.uiState.test {
            // 1. Initial emission must be Loading
            val loadingState = awaitItem()
            assertTrue("Expected initial state to be loading", loadingState.isLoading)
            
            // 2. Subsequent emission must be Success with populated data
            val successState = awaitItem()
            assertEquals("Expected all mock transactions to be present", mockTransactions.size, successState.transactions.size)
            // 200 (Food) + 1500 (Utilities) = 1700. CC Payment (1000) should be excluded.
            assertEquals("Expected spending to exclude internal transfers", 1700.0, successState.totalSpent, 0.0)
            assertEquals("Expected UI state to no longer be loading", false, successState.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
