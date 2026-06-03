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
        val currentMonthBillingDate = calculateSafeDate(today.year, today.monthValue, billingDay)

        // If today is on or before the current month's billing date, 
        // the "previous" statement closed last month.
        // If today is after the billing date, the "previous" statement closed this month.
        val windowEnd = if (today.isAfter(currentMonthBillingDate)) {
            currentMonthBillingDate
        } else {
            val previousMonth = today.minusMonths(1)
            calculateSafeDate(previousMonth.year, previousMonth.monthValue, billingDay)
        }

        // Start date is one month and one day before the end date
        // Note: minusMonths(1) already handles day-of-month truncation defensively
        val windowStart = windowEnd.minusMonths(1).plusDays(1)
        
        return Pair(windowStart, windowEnd)
    }

    /**
     * Creates a [LocalDate] while ensuring the [day] does not exceed the maximum 
     * valid day for the given [year] and [month].
     */
    private fun calculateSafeDate(year: Int, month: Int, day: Int): LocalDate {
        val baseDate = LocalDate.of(year, month, 1)
        val maxDay = baseDate.with(TemporalAdjusters.lastDayOfMonth()).dayOfMonth
        return baseDate.withDayOfMonth(if (day > maxDay) maxDay else day)
    }
}
