package com.karlvcrisostomo.financialmatrix.features.analytics.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {

    @Query("""
        WITH RECURSIVE
          min_val(d) AS (
            SELECT MIN(d) FROM (
              SELECT MIN(date) as d FROM transactions
              UNION ALL
              SELECT MIN(date) FROM income
            )
          ),
          start_date(d) AS (
            SELECT IFNULL(d, julianday('now') - 2440587.5) FROM min_val
          ),
          dates(date) AS (
            SELECT date(d + 2440587.5, 'weekday 0', '-6 days') FROM start_date
            UNION ALL
            SELECT date(date, '+7 days') FROM dates WHERE date < date('now')
          ),
          expense_agg AS (
            SELECT 
                date(date + 2440587.5, 'weekday 0', '-6 days') as week_start,
                SUM(CAST(amount AS REAL)) as total
            FROM transactions
            WHERE category != 'CC Payment'
            GROUP BY week_start
          ),
          income_agg AS (
            SELECT 
                date(date + 2440587.5, 'weekday 0', '-6 days') as week_start,
                SUM(CAST(amount AS REAL)) as total
            FROM income
            GROUP BY week_start
          )
        SELECT 
            d.date as interval,
            IFNULL(e.total, 0) as totalExpenses,
            IFNULL(i.total, 0) as totalIncome
        FROM dates d
        LEFT JOIN expense_agg e ON d.date = e.week_start
        LEFT JOIN income_agg i ON d.date = i.week_start
        ORDER BY d.date DESC
        LIMIT 12
    """)
    fun getWeeklyAnalytics(): Flow<List<AnalyticsDataPoint>>

    @Query("""
        WITH RECURSIVE
          min_val(d) AS (
            SELECT MIN(d) FROM (
              SELECT MIN(date) as d FROM transactions
              UNION ALL
              SELECT MIN(date) FROM income
            )
          ),
          start_date(d) AS (
            SELECT IFNULL(d, julianday('now') - 2440587.5) FROM min_val
          ),
          dates(date) AS (
            SELECT date(d + 2440587.5, 'start of month') FROM start_date
            UNION ALL
            SELECT date(date, '+1 month') FROM dates WHERE date < date('now', 'start of month')
          ),
          expense_agg AS (
            SELECT 
                strftime('%Y-%m-01', date + 2440587.5) as month_start,
                SUM(CAST(amount AS REAL)) as total
            FROM transactions
            WHERE category != 'CC Payment'
            GROUP BY month_start
          ),
          income_agg AS (
            SELECT 
                strftime('%Y-%m-01', date + 2440587.5) as month_start,
                SUM(CAST(amount AS REAL)) as total
            FROM income
            GROUP BY month_start
          )
        SELECT 
            d.date as interval,
            IFNULL(e.total, 0) as totalExpenses,
            IFNULL(i.total, 0) as totalIncome
        FROM dates d
        LEFT JOIN expense_agg e ON d.date = e.month_start
        LEFT JOIN income_agg i ON d.date = i.month_start
        ORDER BY d.date DESC
        LIMIT 12
    """)
    fun getMonthlyAnalytics(): Flow<List<AnalyticsDataPoint>>

    @Query("""
        WITH RECURSIVE
          min_val(d) AS (
            SELECT MIN(d) FROM (
              SELECT MIN(date) as d FROM transactions
              UNION ALL
              SELECT MIN(date) FROM income
            )
          ),
          start_date(d) AS (
            SELECT IFNULL(d, julianday('now') - 2440587.5) FROM min_val
          ),
          dates(date) AS (
            SELECT date(d + 2440587.5, 'start of year') FROM start_date
            UNION ALL
            SELECT date(date, '+1 year') FROM dates WHERE date < date('now', 'start of year')
          ),
          expense_agg AS (
            SELECT 
                strftime('%Y-01-01', date + 2440587.5) as year_start,
                SUM(CAST(amount AS REAL)) as total
            FROM transactions
            WHERE category != 'CC Payment'
            GROUP BY year_start
          ),
          income_agg AS (
            SELECT 
                strftime('%Y-01-01', date + 2440587.5) as year_start,
                SUM(CAST(amount AS REAL)) as total
            FROM income
            GROUP BY year_start
          )
        SELECT 
            d.date as interval,
            IFNULL(e.total, 0) as totalExpenses,
            IFNULL(i.total, 0) as totalIncome
        FROM dates d
        LEFT JOIN expense_agg e ON d.date = e.year_start
        LEFT JOIN income_agg i ON d.date = i.year_start
        ORDER BY d.date DESC
        LIMIT 5
    """)
    fun getYearlyAnalytics(): Flow<List<AnalyticsDataPoint>>

    @Query("""
        SELECT 
            IFNULL(strftime('%Y-%m-01', date + 2440587.5), date('now', 'start of month')) as interval,
            category,
            SUM(CAST(amount AS REAL)) as totalAmount
        FROM transactions
        WHERE category != 'CC Payment'
        GROUP BY interval, category
        ORDER BY interval DESC
    """)
    fun getMonthlyCategoryAggregation(): Flow<List<CategoryAggregation>>

    @Query("""
        SELECT 
            IFNULL(date(date + 2440587.5, 'weekday 0', '-6 days'), date('now', 'weekday 0', '-6 days')) as interval,
            category,
            SUM(CAST(amount AS REAL)) as totalAmount
        FROM transactions
        WHERE category != 'CC Payment'
        GROUP BY interval, category
        ORDER BY interval DESC
    """)
    fun getWeeklyCategoryAggregation(): Flow<List<CategoryAggregation>>

    @Query("""
        SELECT 
            IFNULL(date(date + 2440587.5, 'start of year'), date('now', 'start of year')) as interval,
            category,
            SUM(CAST(amount AS REAL)) as totalAmount
        FROM transactions
        WHERE category != 'CC Payment'
        GROUP BY interval, category
        ORDER BY interval DESC
    """)
    fun getYearlyCategoryAggregation(): Flow<List<CategoryAggregation>>
}
