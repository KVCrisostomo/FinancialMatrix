package com.karlvcrisostomo.financialmatrix.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StatementCycleCalculatorTest {

    @Test
    fun `regular month - today is before billing day`() {
        val billingDay = 15
        val today = LocalDate.of(2026, 6, 10)
        
        // Billing date for June is June 15. Since today is June 10,
        // previous statement closed in May.
        // May billing date is May 15.
        // Window: April 16 to May 15.
        val (start, end) = StatementCycleCalculator.calculatePreviousStatementWindow(billingDay, today)
        
        assertEquals(LocalDate.of(2026, 4, 16), start)
        assertEquals(LocalDate.of(2026, 5, 15), end)
    }

    @Test
    fun `regular month - today is after billing day`() {
        val billingDay = 15
        val today = LocalDate.of(2026, 6, 20)
        
        // Billing date for June is June 15. Since today is June 20,
        // previous statement closed in June.
        // Window: May 16 to June 15.
        val (start, end) = StatementCycleCalculator.calculatePreviousStatementWindow(billingDay, today)
        
        assertEquals(LocalDate.of(2026, 5, 16), start)
        assertEquals(LocalDate.of(2026, 6, 15), end)
    }

    @Test
    fun `short month - February leap year - billing day 31`() {
        val billingDay = 31
        val today = LocalDate.of(2024, 3, 10) // 2024 is a leap year
        
        // March billing date is March 31. Today is March 10.
        // Previous statement closed in February.
        // February 2024 has 29 days. Billing date clamped to 29.
        // Window: Jan 30 to Feb 29 (Wait, Feb 29 - 1 month + 1 day = Jan 30)
        val (start, end) = StatementCycleCalculator.calculatePreviousStatementWindow(billingDay, today)
        
        assertEquals(LocalDate.of(2024, 2, 29), end)
        assertEquals(LocalDate.of(2024, 1, 30), start)
    }

    @Test
    fun `short month - February non-leap year - billing day 30`() {
        val billingDay = 30
        val today = LocalDate.of(2023, 3, 5) // 2023 is not a leap year
        
        // Previous statement closed in February.
        // February 2023 has 28 days. Billing date clamped to 28.
        val (start, end) = StatementCycleCalculator.calculatePreviousStatementWindow(billingDay, today)
        
        assertEquals(LocalDate.of(2023, 2, 28), end)
        assertEquals(LocalDate.of(2023, 1, 29), start)
    }

    @Test
    fun `billing day on 1st of month`() {
        val billingDay = 1
        val today = LocalDate.of(2026, 6, 1)
        
        // Today is June 1. Billing date is June 1.
        // Since today is NOT AFTER June 1, previous statement closed in May.
        // May billing date is May 1.
        // Window: April 2 to May 1.
        val (start, end) = StatementCycleCalculator.calculatePreviousStatementWindow(billingDay, today)
        
        assertEquals(LocalDate.of(2026, 5, 1), end)
        assertEquals(LocalDate.of(2026, 4, 2), start)
    }

    @Test
    fun `year boundary - January today before billing day`() {
        val billingDay = 15
        val today = LocalDate.of(2026, 1, 10)
        
        // Previous statement closed in Dec 2025.
        val (start, end) = StatementCycleCalculator.calculatePreviousStatementWindow(billingDay, today)
        
        assertEquals(LocalDate.of(2025, 12, 15), end)
        assertEquals(LocalDate.of(2025, 11, 16), start)
    }

    @Test
    fun `clamping - billing day 31 in April`() {
        val billingDay = 31
        val today = LocalDate.of(2026, 5, 5)
        
        // Previous statement closed in April. April has 30 days.
        val (start, end) = StatementCycleCalculator.calculatePreviousStatementWindow(billingDay, today)
        
        assertEquals(LocalDate.of(2026, 4, 30), end)
        assertEquals(LocalDate.of(2026, 3, 31), start) // March 31 - 1 month = Feb 28/29? No, LocalDate logic
        // 2026-04-30 minus 1 month = 2026-03-30. Plus 1 day = 2026-03-31. Correct.
    }
}
