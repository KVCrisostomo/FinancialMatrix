package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import androidx.work.WorkManager
import app.cash.turbine.test
import com.karlvcrisostomo.financialmatrix.core.data.UserPreferences
import com.karlvcrisostomo.financialmatrix.core.data.UserPreferencesRepository
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringTransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository
import com.karlvcrisostomo.financialmatrix.domain.usecase.ValidateTransactionSourceUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private val repository: TransactionRepository = mockk(relaxed = true)
    private val incomeRepository: IncomeRepository = mockk(relaxed = true)
    private val recurringRepo: RecurringTransactionRepository = mockk(relaxed = true)
    private val preferencesRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockTransactions = listOf(
        TransactionEntity(1, "Jollibee", BigDecimal("200.00"), LocalDate.now(), "Food", true, "Primary"),
        TransactionEntity(2, "Electric Bill", BigDecimal("1500.00"), LocalDate.now(), "Utilities", false, "Primary"),
        TransactionEntity(3, "Credit Card Payment", BigDecimal("1000.00"), LocalDate.now(), "CC Payment", false, "Primary")
    )

    private val mockIncome = listOf(
        IncomeEntity(1, "Salary", BigDecimal("5000.00"), LocalDate.now()),
        IncomeEntity(2, "Freelance", BigDecimal("1200.00"), LocalDate.now()),
        IncomeEntity(3, "Old Job", BigDecimal("1000.00"), LocalDate.now().minusMonths(2))
    )

    private val mockPreferences = UserPreferences("₱", false, BigDecimal("5000.00"))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllTransactions() } returns flowOf(mockTransactions)
        every { repository.getMonthlyTotalSpent() } returns flowOf(BigDecimal("1700.00"))
        every { repository.getMonthlyCashSpent() } returns flowOf(BigDecimal("1500.00"))
        every { repository.getMonthlyCreditSpent() } returns flowOf(BigDecimal("200.00"))
        every { repository.getMonthlyCategoryAmounts() } returns flowOf(mapOf("Food" to BigDecimal("200.00"), "Utilities" to BigDecimal("1500.00")))
        every { incomeRepository.getAllIncome() } returns flowOf(mockIncome)
        every { incomeRepository.getMonthlyTotalIncome() } returns flowOf(BigDecimal("6200.00"))
        every { recurringRepo.getAllRecurringTransactions() } returns flowOf(emptyList())
        every { preferencesRepository.userPreferencesFlow } returns flowOf(mockPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is correct and excludes CC Payment from totals`() = runTest(testDispatcher) {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, ValidateTransactionSourceUseCase(), testDispatcher)

        viewModel.uiState.test {
            // UnconfinedTestDispatcher should cause immediate emission
            val state = awaitItem()
            
            // Check if it's loading or already success
            val finalState = if (state.isLoading) awaitItem() else state
            
            assertEquals(mockTransactions.size, finalState.transactions.size)
            assertEquals(BigDecimal("1700.00"), finalState.totalSpent)
            assertEquals(BigDecimal("6200.00"), finalState.totalIncome)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTransaction for CC Payment calls insertCreditCardPayment`() = runTest(testDispatcher) {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, ValidateTransactionSourceUseCase(), testDispatcher)
        val payment = TransactionEntity(4, "Card Pay", BigDecimal("500.00"), LocalDate.now(), "CC Payment", false, "Primary", targetCreditCardId = 1L)

        viewModel.uiState.test {
            val state = awaitItem()
            if (state.isLoading) awaitItem()

            viewModel.addTransaction(payment)

            coVerify { repository.insertCreditCardPayment(payment, 1L) }
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTransaction throws error when paying CC with another CC`() = runTest(testDispatcher) {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, ValidateTransactionSourceUseCase(), testDispatcher)
        val invalidPayment = TransactionEntity(4, "Invalid Pay", BigDecimal("500.00"), LocalDate.now(), "CC Payment", true, "Visa")

        viewModel.uiState.test {
            val state = awaitItem()
            if (state.isLoading) awaitItem()

            viewModel.addTransaction(invalidPayment)
            val errorState = awaitItem()
            
            assertNotNull(errorState.errorMessage)
            assertEquals("Credit card payments cannot be funded by another credit card.", errorState.errorMessage)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTransaction for Expense with CC calls insertExpenseWithBalanceUpdate with correct ID`() = runTest(testDispatcher) {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, ValidateTransactionSourceUseCase(), testDispatcher)
        val expense = TransactionEntity(5, "Grocery", BigDecimal("300.00"), LocalDate.now(), "Food", true, "Visa", targetCreditCardId = 2L)

        viewModel.uiState.test {
            val state = awaitItem()
            if (state.isLoading) awaitItem()

            viewModel.addTransaction(expense)

            coVerify { repository.insertExpenseWithBalanceUpdate(expense, 2L) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteTransaction for CC Expense calls deleteTransactionWithBalanceUpdate`() = runTest(testDispatcher) {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, ValidateTransactionSourceUseCase(), testDispatcher)
        val expense = TransactionEntity(10, "Grocery", BigDecimal("300.00"), LocalDate.now(), "Food", true, "Visa", targetCreditCardId = 2L)

        viewModel.deleteTransaction(expense)

        coVerify { repository.deleteTransactionWithBalanceUpdate(expense) }
    }

    @Test
    fun `deleteTransaction for CC Payment calls deleteTransactionWithBalanceUpdate`() = runTest(testDispatcher) {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, ValidateTransactionSourceUseCase(), testDispatcher)
        val payment = TransactionEntity(11, "Payment", BigDecimal("500.00"), LocalDate.now(), "CC Payment", false, "Primary", targetCreditCardId = 1L)

        viewModel.deleteTransaction(payment)

        coVerify { repository.deleteTransactionWithBalanceUpdate(payment) }
    }

    @Test
    fun `deleteTransaction for Standard Expense calls standard delete`() = runTest(testDispatcher) {
        val viewModel = TransactionViewModel(repository, incomeRepository, recurringRepo, preferencesRepository, workManager, ValidateTransactionSourceUseCase(), testDispatcher)
        val expense = TransactionEntity(12, "Cash Snack", BigDecimal("50.00"), LocalDate.now(), "Food", false, "Primary")

        viewModel.deleteTransaction(expense)

        coVerify { repository.deleteTransaction(expense) }
    }
}
