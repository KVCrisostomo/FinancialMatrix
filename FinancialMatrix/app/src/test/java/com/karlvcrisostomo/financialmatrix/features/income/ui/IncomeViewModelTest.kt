package com.karlvcrisostomo.financialmatrix.features.income.ui

import app.cash.turbine.test
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository
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
class IncomeViewModelTest {

    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val incomeRepository: IncomeRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val mockTransactions = listOf(
        TransactionEntity(1, "Jollibee", 200.0, LocalDate.now(), "Food", true, "Primary"),
        TransactionEntity(2, "Electric Bill", 1500.0, LocalDate.now(), "Utilities", false, "Primary"),
        TransactionEntity(3, "Credit Card Payment", 1000.0, LocalDate.now(), "CC Payment", false, "Primary")
    )

    private val mockIncome = listOf(
        IncomeEntity(1, "Salary", 5000.0, LocalDate.now()),
        IncomeEntity(2, "Freelance", 1200.0, LocalDate.now()),
        IncomeEntity(3, "Old Job", 1000.0, LocalDate.now().minusMonths(2))
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { transactionRepository.getAllTransactions() } returns flowOf(mockTransactions)
        every { incomeRepository.getAllIncome() } returns flowOf(mockIncome)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `savingsUiState emits loading then success with correct multi-emission arithmetic`() = runTest {
        val viewModel = IncomeViewModel(transactionRepository, incomeRepository, testDispatcher)

        viewModel.savingsUiState.test {
            // 1. Assert initial emission is Loading
            val loadingState = awaitItem()
            assertTrue("Expected initial emission to be Loading", loadingState.isLoading)
            
            // 2. Assert subsequent emission is Success with correct calculations
            val successState = awaitItem()
            
            // Total Income = 5000 + 1200 = 6200.0 (Old Job excluded)
            // Total Spent (excluding CC Payment) = 200 + 1500 = 1700.0
            // Net Savings = 6200 - 1700 = 4500.0
            assertEquals("Incorrect Net Savings calculation", 4500.0, successState.netSavings, 0.0)
            
            // Savings Rate = (4500 / 6200) * 100 approx 72.58%
            assertEquals("Incorrect Savings Rate calculation", 72.58, successState.savingsRate, 0.01)
            assertEquals("Success state should not be loading", false, successState.isLoading)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
