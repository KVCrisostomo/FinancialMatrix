package com.karlvcrisostomo.financialmatrix.features.analytics.data

import com.karlvcrisostomo.financialmatrix.domain.usecase.CategorySpending
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class AnalyticsRepository(private val analyticsDao: AnalyticsDao) {

    fun getWeeklyAnalytics(): Flow<List<AnalyticsDataPoint>> = combine(
        analyticsDao.getAllTransactions(),
        analyticsDao.getAllIncome()
    ) { transactions, income ->
        val now = LocalDate.now()
        val startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        
        (0 until 12).map { i ->
            val weekStart = startOfWeek.minusWeeks(i.toLong())
            val weekEnd = weekStart.plusDays(6)
            
            val totalExp = transactions.filter { it.date in weekStart..weekEnd }
                .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
            val totalInc = income.filter { it.date in weekStart..weekEnd }
                .fold(BigDecimal.ZERO) { acc, inc -> acc.add(inc.amount) }
                
            AnalyticsDataPoint(weekStart.toString(), totalExp, totalInc)
        }
    }

    fun getMonthlyAnalytics(): Flow<List<AnalyticsDataPoint>> = combine(
        analyticsDao.getAllTransactions(),
        analyticsDao.getAllIncome()
    ) { transactions, income ->
        val now = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
        
        (0 until 12).map { i ->
            val monthStart = now.minusMonths(i.toLong())
            val monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth())
            
            val totalExp = transactions.filter { it.date in monthStart..monthEnd }
                .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
            val totalInc = income.filter { it.date in monthStart..monthEnd }
                .fold(BigDecimal.ZERO) { acc, inc -> acc.add(inc.amount) }
                
            AnalyticsDataPoint(monthStart.toString(), totalExp, totalInc)
        }
    }

    fun getYearlyAnalytics(): Flow<List<AnalyticsDataPoint>> = combine(
        analyticsDao.getAllTransactions(),
        analyticsDao.getAllIncome()
    ) { transactions, income ->
        val now = LocalDate.now().with(TemporalAdjusters.firstDayOfYear())
        
        (0 until 5).map { i ->
            val yearStart = now.minusYears(i.toLong())
            val yearEnd = yearStart.with(TemporalAdjusters.lastDayOfYear())
            
            val totalExp = transactions.filter { it.date in yearStart..yearEnd }
                .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
            val totalInc = income.filter { it.date in yearStart..yearEnd }
                .fold(BigDecimal.ZERO) { acc, inc -> acc.add(inc.amount) }
                
            AnalyticsDataPoint(yearStart.toString(), totalExp, totalInc)
        }
    }

    fun getWeeklyCategoryAggregation(): Flow<List<CategoryAggregation>> = 
        analyticsDao.getAllTransactions().combine(analyticsDao.getAllIncome()) { transactions, _ ->
            val now = LocalDate.now()
            val startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            
            (0 until 12).flatMap { i ->
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

    fun getMonthlyCategoryAggregation(): Flow<List<CategoryAggregation>> = 
        analyticsDao.getAllTransactions().combine(analyticsDao.getAllIncome()) { transactions, _ ->
            val now = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
            
            (0 until 12).flatMap { i ->
                val monthStart = now.minusMonths(i.toLong())
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

    fun getYearlyCategoryAggregation(): Flow<List<CategoryAggregation>> = 
        analyticsDao.getAllTransactions().combine(analyticsDao.getAllIncome()) { transactions, _ ->
            val now = LocalDate.now().with(TemporalAdjusters.firstDayOfYear())
            
            (0 until 5).flatMap { i ->
                val yearStart = now.minusYears(i.toLong())
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
