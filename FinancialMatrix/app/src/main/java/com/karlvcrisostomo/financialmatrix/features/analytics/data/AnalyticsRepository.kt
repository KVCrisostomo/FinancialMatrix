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
        
        return combine(
            analyticsDao.getTransactionsInRange(startOfRange, endOfRange),
            analyticsDao.getIncomeInRange(startOfRange, endOfRange)
        ) { transactions, income ->
            val startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            
            (0 until 6).map { i ->
                val weekStart = startOfWeek.minusWeeks(i.toLong())
                val weekEnd = weekStart.plusDays(6)
                
                val totalExp = transactions.filter { it.date in weekStart..weekEnd }
                    .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
                val totalInc = income.filter { it.date in weekStart..weekEnd }
                    .fold(BigDecimal.ZERO) { acc, inc -> acc.add(inc.amount) }
                    
                AnalyticsDataPoint(weekStart.toString(), totalExp, totalInc)
            }
        }
    }

    fun getMonthlyAnalytics(): Flow<List<AnalyticsDataPoint>> {
        val now = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
        val startOfRange = now.minusMonths(5)
        val endOfRange = now.with(TemporalAdjusters.lastDayOfMonth())
        
        return combine(
            analyticsDao.getTransactionsInRange(startOfRange, endOfRange),
            analyticsDao.getIncomeInRange(startOfRange, endOfRange)
        ) { transactions, income ->
            val firstOfMonth = now
            
            (0 until 6).map { i ->
                val monthStart = firstOfMonth.minusMonths(i.toLong())
                val monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth())
                
                val totalExp = transactions.filter { it.date in monthStart..monthEnd }
                    .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
                val totalInc = income.filter { it.date in monthStart..monthEnd }
                    .fold(BigDecimal.ZERO) { acc, inc -> acc.add(inc.amount) }
                    
                AnalyticsDataPoint(monthStart.toString(), totalExp, totalInc)
            }
        }
    }

    fun getYearlyAnalytics(): Flow<List<AnalyticsDataPoint>> {
        val now = LocalDate.now().with(TemporalAdjusters.firstDayOfYear())
        val startOfRange = now.minusYears(5)
        val endOfRange = now.with(TemporalAdjusters.lastDayOfYear())
        
        return combine(
            analyticsDao.getTransactionsInRange(startOfRange, endOfRange),
            analyticsDao.getIncomeInRange(startOfRange, endOfRange)
        ) { transactions, income ->
            val firstOfYear = now
            
            (0 until 6).map { i ->
                val yearStart = firstOfYear.minusYears(i.toLong())
                val yearEnd = yearStart.with(TemporalAdjusters.lastDayOfYear())
                
                val totalExp = transactions.filter { it.date in yearStart..yearEnd }
                    .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
                val totalInc = income.filter { it.date in yearStart..yearEnd }
                    .fold(BigDecimal.ZERO) { acc, inc -> acc.add(inc.amount) }
                    
                AnalyticsDataPoint(yearStart.toString(), totalExp, totalInc)
            }
        }
    }

    fun getWeeklyCategoryAggregation(): Flow<List<CategoryAggregation>> {
        val now = LocalDate.now()
        val startOfRange = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(5)
        val endOfRange = now
        
        return analyticsDao.getTransactionsInRange(startOfRange, endOfRange).combine(analyticsDao.getIncomeInRange(startOfRange, endOfRange)) { transactions, _ ->
            val startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            
            (0 until 6).flatMap { i ->
                val weekStart = startOfWeek.minusWeeks(i.toLong())
                val weekEnd = weekStart.plusDays(6)
                
                transactions.filter { it.date in weekStart..weekEnd }
                    .groupBy { it.category }
                    .map { (cat, list) ->
                        CategoryAggregation(
                            interval = weekStart.toString(),
                            category = cat,
                            totalAmount = list.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
                        )
                    }
            }
        }
    }

    fun getMonthlyCategoryAggregation(): Flow<List<CategoryAggregation>> {
        val now = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
        val startOfRange = now.minusMonths(5)
        val endOfRange = now.with(TemporalAdjusters.lastDayOfMonth())
        
        return analyticsDao.getTransactionsInRange(startOfRange, endOfRange).combine(analyticsDao.getIncomeInRange(startOfRange, endOfRange)) { transactions, _ ->
            val firstOfMonth = now
            
            (0 until 6).flatMap { i ->
                val monthStart = firstOfMonth.minusMonths(i.toLong())
                val monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth())
                
                transactions.filter { it.date in monthStart..monthEnd }
                    .groupBy { it.category }
                    .map { (cat, list) ->
                        CategoryAggregation(
                            interval = monthStart.toString(),
                            category = cat,
                            totalAmount = list.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
                        )
                    }
            }
        }
    }

    fun getYearlyCategoryAggregation(): Flow<List<CategoryAggregation>> {
        val now = LocalDate.now().with(TemporalAdjusters.firstDayOfYear())
        val startOfRange = now.minusYears(5)
        val endOfRange = now.with(TemporalAdjusters.lastDayOfYear())
        
        return analyticsDao.getTransactionsInRange(startOfRange, endOfRange).combine(analyticsDao.getIncomeInRange(startOfRange, endOfRange)) { transactions, _ ->
            val firstOfYear = now
            
            (0 until 6).flatMap { i ->
                val yearStart = firstOfYear.minusYears(i.toLong())
                val yearEnd = yearStart.with(TemporalAdjusters.lastDayOfYear())
                
                transactions.filter { it.date in yearStart..yearEnd }
                    .groupBy { it.category }
                    .map { (cat, list) ->
                        CategoryAggregation(
                            interval = yearStart.toString(),
                            category = cat,
                            totalAmount = list.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
                        )
                    }
            }
        }
    }
}
