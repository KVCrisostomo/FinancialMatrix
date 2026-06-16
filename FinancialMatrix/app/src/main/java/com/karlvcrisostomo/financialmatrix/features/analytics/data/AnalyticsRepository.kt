package com.karlvcrisostomo.financialmatrix.features.analytics.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class AnalyticsRepository(private val analyticsDao: AnalyticsDao) {

    fun getWeeklyAnalytics(): Flow<List<AnalyticsDataPoint>> {
        val now = LocalDate.now()
        val startOfRange = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(5)
        val endOfRange = now
        return analyticsDao.getAggregatedAnalytics(startOfRange, endOfRange)
    }

    fun getMonthlyAnalytics(): Flow<List<AnalyticsDataPoint>> {
        val now = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
        val startOfRange = now.minusMonths(5)
        val endOfRange = now.with(TemporalAdjusters.lastDayOfMonth())
        return analyticsDao.getAggregatedAnalytics(startOfRange, endOfRange)
    }

    fun getYearlyAnalytics(): Flow<List<AnalyticsDataPoint>> {
        val now = LocalDate.now().with(TemporalAdjusters.firstDayOfYear())
        val startOfRange = now.minusYears(5)
        val endOfRange = now.with(TemporalAdjusters.lastDayOfYear())
        return analyticsDao.getAggregatedAnalytics(startOfRange, endOfRange)
    }

    fun getWeeklyCategoryAggregation(): Flow<List<CategoryAggregation>> {
        val now = LocalDate.now()
        val startOfRange = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(5)
        val endOfRange = now
        return analyticsDao.getCategoryAggregation(startOfRange, endOfRange)
    }

    fun getMonthlyCategoryAggregation(): Flow<List<CategoryAggregation>> {
        val now = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
        val startOfRange = now.minusMonths(5)
        val endOfRange = now.with(TemporalAdjusters.lastDayOfMonth())
        return analyticsDao.getCategoryAggregation(startOfRange, endOfRange)
    }

    fun getYearlyCategoryAggregation(): Flow<List<CategoryAggregation>> {
        val now = LocalDate.now().with(TemporalAdjusters.firstDayOfYear())
        val startOfRange = now.minusYears(5)
        val endOfRange = now.with(TemporalAdjusters.lastDayOfYear())
        return analyticsDao.getCategoryAggregation(startOfRange, endOfRange)
    }
}
