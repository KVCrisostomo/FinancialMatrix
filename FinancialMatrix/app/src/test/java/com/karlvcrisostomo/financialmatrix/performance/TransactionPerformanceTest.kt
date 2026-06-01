package com.karlvcrisostomo.financialmatrix.performance

import app.cash.turbine.test
import com.karlvcrisostomo.financialmatrix.core.data.UserPreferences
import com.karlvcrisostomo.financialmatrix.core.data.UserPreferencesRepository
import com.karlvcrisostomo.financialmatrix.core.util.toCsvString
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.TransactionSortOrder
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.TransactionViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import androidx.work.WorkManager
import kotlin.system.measureTimeMillis

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionPerformanceTest {

    private val repository: TransactionRepository = mockk(relaxed = true)
    private val preferencesRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val largeDatasetSize = 10_000
    private val largeDataset = List(largeDatasetSize) { i ->
        TransactionEntity(
            id = i.toLong(),
            description = "Transaction $i",
            amount = (1..1000).random().toDouble(),
            date = LocalDate.now().minusDays((0..365).random().toLong()),
            category = listOf("Food", "Utilities", "Transport", "Entertainment", "Other").random(),
            isCreditCard = (0..1).random() == 1,
            accountName = "Primary"
        )
    }

    private val mockPreferences = UserPreferences("₱", false, 5000.0)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllTransactions() } returns MutableStateFlow(largeDataset)
        every { preferencesRepository.userPreferencesFlow } returns MutableStateFlow(mockPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search and filter on 10,000 transactions executes within performance bounds`() = runTest {
        val viewModel = TransactionViewModel(repository, preferencesRepository, workManager)

        viewModel.uiState.test {
            awaitItem() // Initial load

            val executionTime = measureTimeMillis {
                viewModel.updateSearchQuery("Transaction 500")
                val state = awaitItem()
                assertTrue(state.transactions.isNotEmpty())
            }

            println("Search/Filter Execution Time for $largeDatasetSize items: ${executionTime}ms")
            // Bound check: usually < 100ms on JVM for simple list filtering of 10k items
            assertTrue("Filtering took too long: ${executionTime}ms", executionTime < 200)
        }
    }

    @Test
    fun `sorting 10,000 transactions executes within performance bounds`() = runTest {
        val viewModel = TransactionViewModel(repository, preferencesRepository, workManager)

        viewModel.uiState.test {
            awaitItem() // Initial load

            val executionTime = measureTimeMillis {
                viewModel.updateSortOrder(TransactionSortOrder.HIGHEST_AMOUNT)
                val state = awaitItem()
                assertTrue(state.transactions[0].amount >= state.transactions[1].amount)
            }

            println("Sort Execution Time for $largeDatasetSize items: ${executionTime}ms")
            assertTrue("Sorting took too long: ${executionTime}ms", executionTime < 300)
        }
    }

    @Test
    fun `CSV serialization of 10,000 transactions executes within performance bounds`() {
        val executionTime = measureTimeMillis {
            val csv = largeDataset.toCsvString()
            assertTrue(csv.isNotBlank())
        }

        println("CSV Serialization Execution Time for $largeDatasetSize items: ${executionTime}ms")
        assertTrue("CSV Export took too long: ${executionTime}ms", executionTime < 500)
    }
}
