package com.karlvcrisostomo.financialmatrix.domain.util

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object StatementCycleCalculator {

    /**
     * Calculates the statement window for the PREVIOUS statement period relative to [today].
     * The statement period ends on the [billingDay] of the month.
     * 
     * @param billingDay The day of the month the statement closes (1-31).
     * @param today The reference date for the calculation.
     * @return A Pair of (startDate, endDate) representing the statement window.
     */
    fun calculatePreviousStatementWindow(
        billingDay: Int,
        today: LocalDate
    ): Pair<LocalDate, LocalDate> {
        // Find the billing date for the current month, defensively clamped
        val currentMonthBillingDate = today.with(TemporalAdjusters.lastDayOfMonth()).let { lastDay ->
            val day = if (billingDay <= lastDay.dayOfMonth) billingDay else lastDay.dayOfMonth
            today.withDayOfMonth(day)
        }

        // If today is on or before the current month's billing date, 
        // the "previous" statement closed last month.
        // If today is after the billing date, the "previous" statement closed this month.
        val windowEnd = if (today.isAfter(currentMonthBillingDate)) {
            currentMonthBillingDate
        } else {
            val previousMonth = today.minusMonths(1)
            val lastDayPrev = previousMonth.with(TemporalAdjusters.lastDayOfMonth())
            val day = if (billingDay <= lastDayPrev.dayOfMonth) billingDay else lastDayPrev.dayOfMonth
            previousMonth.withDayOfMonth(day)
        }

        val windowStart = windowEnd.minusMonths(1).plusDays(1)
        
        return Pair(windowStart, windowEnd)
    }
}
