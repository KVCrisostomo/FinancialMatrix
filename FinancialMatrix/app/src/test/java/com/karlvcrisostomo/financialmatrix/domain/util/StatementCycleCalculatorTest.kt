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
        val (start, end) = StatementCycleCalculator.calculatePreviousStatementWindow(billingDay, today)
        
        assertEquals(LocalDate.of(2024, 2, 29), end)
        assertEquals(LocalDate.of(2024, 1, 30), start)
    }

    @Test
    fun `calculateDueDate - simple offset`() {
        val billingDate = LocalDate.of(2026, 6, 15)
        val offset = 20
        val dueDate = StatementCycleCalculator.calculateDueDate(billingDate, offset)
        
        assertEquals(LocalDate.of(2026, 7, 5), dueDate)
    }

    @Test
    fun `calculateDueDate - year boundary`() {
        val billingDate = LocalDate.of(2025, 12, 20)
        val offset = 15
        val dueDate = StatementCycleCalculator.calculateDueDate(billingDate, offset)
        
        assertEquals(LocalDate.of(2026, 1, 4), dueDate)
    }

    @Test
    fun `calculateDueDate - leap year February`() {
        val billingDate = LocalDate.of(2024, 2, 10)
        val offset = 20
        val dueDate = StatementCycleCalculator.calculateDueDate(billingDate, offset)
        
        assertEquals(LocalDate.of(2024, 3, 1), dueDate)
    }

    @Test
    fun `calculateDueDate - non-leap year February`() {
        val billingDate = LocalDate.of(2023, 2, 10)
        val offset = 20
        val dueDate = StatementCycleCalculator.calculateDueDate(billingDate, offset)
        
        assertEquals(LocalDate.of(2023, 3, 2), dueDate)
    }
}
