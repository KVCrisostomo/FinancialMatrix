package com.karlvcrisostomo.financialmatrix.features.analytics.ui

import app.cash.turbine.test
import com.karlvcrisostomo.financialmatrix.features.analytics.data.AnalyticsDataPoint
import com.karlvcrisostomo.financialmatrix.features.analytics.data.AnalyticsRepository
import com.karlvcrisostomo.financialmatrix.features.analytics.data.CategoryAggregation
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    private val repository = mockk<AnalyticsRepository>()
    private val testDispatcher = StandardTestDispatcher()

    private val mockDataPoints = listOf(
        AnalyticsDataPoint("2026-06-01", BigDecimal("100.00"), BigDecimal("500.00"))
    )
    private val mockCategoryAgg = listOf(
        CategoryAggregation("2026-06-01", "Food", BigDecimal("100.00"))
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getWeeklyAnalytics() } returns flowOf(mockDataPoints)
        every { repository.getMonthlyAnalytics() } returns flowOf(mockDataPoints)
        every { repository.getYearlyAnalytics() } returns flowOf(mockDataPoints)
        every { repository.getWeeklyCategoryAggregation() } returns flowOf(mockCategoryAgg)
        every { repository.getMonthlyCategoryAggregation() } returns flowOf(mockCategoryAgg)
        every { repository.getYearlyCategoryAggregation() } returns flowOf(mockCategoryAgg)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        val viewModel = AnalyticsViewModel(repository)
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `uiState emits success after loading data`() = runTest {
        val viewModel = AnalyticsViewModel(repository)
        
        viewModel.uiState.test {
            // Initial loading
            assertTrue(awaitItem().isLoading)
            
            // Success state
            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertEquals(AnalyticsTimeline.MONTH, successState.timeline)
            assertEquals(mockDataPoints, successState.dataPoints)
            assertTrue(successState.availableCategories.contains("Food"))
            assertTrue(successState.availableCategories.contains("Income"))
        }
    }

    @Test
    fun `toggleCategory updates selectedCategories and uiState`() = runTest {
        val viewModel = AnalyticsViewModel(repository)
        
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success
            
            viewModel.toggleCategory("Food")
            val filteredState = awaitItem()
            assertTrue(filteredState.selectedCategories.contains("Food"))
            assertEquals(1, filteredState.categorySpending.values.flatten().size)
            
            viewModel.toggleCategory("Food")
            val clearedState = awaitItem()
            assertTrue(clearedState.selectedCategories.isEmpty())
        }
    }

    @Test
    fun `updateTimeline changes timeline and fetches new data`() = runTest {
        val viewModel = AnalyticsViewModel(repository)
        
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success (Month default)
            
            viewModel.updateTimeline(AnalyticsTimeline.WEEK)
            val weekState = awaitItem()
            assertEquals(AnalyticsTimeline.WEEK, weekState.timeline)
            
            viewModel.updateTimeline(AnalyticsTimeline.YEAR)
            val yearState = awaitItem()
            assertEquals(AnalyticsTimeline.YEAR, yearState.timeline)
        }
    }
}
