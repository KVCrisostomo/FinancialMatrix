package com.karlvcrisostomo.financialmatrix.features.analytics

import app.cash.turbine.test
import com.karlvcrisostomo.financialmatrix.domain.usecase.GetCategorySpendingUseCase
import com.karlvcrisostomo.financialmatrix.features.analytics.data.CategoryAggregation
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class AnalyticsAggregationTest {

    private val useCase = GetCategorySpendingUseCase()

    @Test
    fun `GetCategorySpendingUseCase excludes CC Payment and filters by selection`() = runTest {
        val rawData = listOf(
            CategoryAggregation("2026-06-01", "Food", BigDecimal("100.00")),
            CategoryAggregation("2026-06-01", "CC Payment", BigDecimal("500.00")),
            CategoryAggregation("2026-06-01", "Utilities", BigDecimal("200.00")),
            CategoryAggregation("2026-07-01", "Food", BigDecimal("150.00"))
        )
        
        val aggregationFlow = flowOf(rawData)
        val selectedCategories = setOf("Food")

        // Test flow-based execute
        useCase.execute(aggregationFlow, selectedCategories).test {
            val result = awaitItem()
            verifyFoodOnly(result)
            cancelAndIgnoreRemainingEvents()
        }

        // Test executeSync
        val syncResult = useCase.executeSync(rawData, selectedCategories)
        verifyFoodOnly(syncResult)
    }

    private fun verifyFoodOnly(result: Map<String, List<com.karlvcrisostomo.financialmatrix.domain.usecase.CategorySpending>>) {
        // Verify CC Payment is excluded
        result.values.flatten().forEach { 
            assertTrue("CC Payment should be excluded", it.category != "CC Payment")
        }

        // Verify only 'Food' is included when selected
        result.values.flatten().forEach {
            assertEquals("Food", it.category)
        }

        // Verify grouping by interval
        assertTrue(result.containsKey("2026-06-01"))
        assertTrue(result.containsKey("2026-07-01"))
        assertEquals(1, result["2026-06-01"]?.size)
        assertEquals(BigDecimal("100.00"), result["2026-06-01"]?.get(0)?.amount)
    }

    @Test
    fun `GetCategorySpendingUseCase returns all except CC Payment when selection is empty`() = runTest {
        val rawData = listOf(
            CategoryAggregation("2026-06-01", "Food", BigDecimal("100.00")),
            CategoryAggregation("2026-06-01", "CC Payment", BigDecimal("500.00")),
            CategoryAggregation("2026-06-01", "Utilities", BigDecimal("200.00"))
        )
        
        val aggregationFlow = flowOf(rawData)
        val selectedCategories = emptySet<String>()

        useCase.execute(aggregationFlow, selectedCategories).test {
            val result = awaitItem()
            
            val juneSpending = result["2026-06-01"]
            assertEquals(2, juneSpending?.size)
            assertTrue(juneSpending?.any { it.category == "Food" } == true)
            assertTrue(juneSpending?.any { it.category == "Utilities" } == true)
            assertTrue(juneSpending?.none { it.category == "CC Payment" } == true)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GetCategorySpendingUseCase handles empty input gracefully`() = runTest {
        val aggregationFlow = flowOf(emptyList<CategoryAggregation>())
        val selectedCategories = emptySet<String>()

        useCase.execute(aggregationFlow, selectedCategories).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
