package com.karlvcrisostomo.financialmatrix.features.analytics.data

import kotlinx.coroutines.flow.Flow

class AnalyticsRepository(private val analyticsDao: AnalyticsDao) {
    fun getWeeklyAnalytics(): Flow<List<AnalyticsDataPoint>> = analyticsDao.getWeeklyAnalytics()
    fun getMonthlyAnalytics(): Flow<List<AnalyticsDataPoint>> = analyticsDao.getMonthlyAnalytics()
    fun getYearlyAnalytics(): Flow<List<AnalyticsDataPoint>> = analyticsDao.getYearlyAnalytics()

    fun getWeeklyCategoryAggregation(): Flow<List<CategoryAggregation>> = analyticsDao.getWeeklyCategoryAggregation()
    fun getMonthlyCategoryAggregation(): Flow<List<CategoryAggregation>> = analyticsDao.getMonthlyCategoryAggregation()
    fun getYearlyCategoryAggregation(): Flow<List<CategoryAggregation>> = analyticsDao.getYearlyCategoryAggregation()
}
