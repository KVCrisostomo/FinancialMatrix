package com.karlvcrisostomo.financialmatrix.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.LocalDate

@RunWith(Parameterized::class)
class StatementCycleCalculatorTest(
    private val billingDate: LocalDate,
    private val offset: Int,
    private val expectedDueDate: LocalDate,
    private val scenarioName: String
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{3}")
        fun data(): Collection<Array<Any>> = listOf(
            // 31-day month (January -> February)
            arrayOf(LocalDate.of(2026, 1, 15), 20, LocalDate.of(2026, 2, 4), "Jan 15 + 20 days"),
            
            // 30-day month (April -> May)
            arrayOf(LocalDate.of(2026, 4, 15), 20, LocalDate.of(2026, 5, 5), "Apr 15 + 20 days"),
            
            // 28-day month (February non-leap year -> March)
            arrayOf(LocalDate.of(2023, 2, 15), 15, LocalDate.of(2023, 3, 2), "Feb 15 + 15 days (Non-Leap)"),
            
            // 29-day month (February leap year -> March)
            arrayOf(LocalDate.of(2024, 2, 15), 15, LocalDate.of(2024, 3, 1), "Feb 15 + 15 days (Leap Year)"),
            
            // Year boundary (December -> January)
            arrayOf(LocalDate.of(2025, 12, 20), 15, LocalDate.of(2026, 1, 4), "Dec 20 + 15 days"),
            
            // Month boundary (Last day of month)
            arrayOf(LocalDate.of(2026, 6, 30), 1, LocalDate.of(2026, 7, 1), "Jun 30 + 1 day")
        )
    }

    @Test
    fun `calculateDueDate asserts temporal safety across variable month lengths`() {
        val actualDueDate = StatementCycleCalculator.calculateDueDate(billingDate, offset)
        assertEquals("Failed temporal safety check: $scenarioName", expectedDueDate, actualDueDate)
    }
}
