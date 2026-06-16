package com.karlvcrisostomo.financialmatrix.features.analytics.data

import androidx.room.Dao
import androidx.room.Query
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDate

@Dao
interface AnalyticsDao {

    @Query("SELECT strftime('%Y-%m-%d', date('1970-01-01', '+' || date || ' days')) as interval, SUM(amount) as totalExpenses, (SELECT SUM(amount) FROM income WHERE strftime('%Y-%m-%d', date('1970-01-01', '+' || date || ' days')) BETWEEN :start AND :end) as totalIncome FROM transactions WHERE category != 'CC Payment' AND date BETWEEN :start AND :end GROUP BY interval ORDER BY interval DESC")
    fun getAggregatedAnalytics(start: LocalDate, end: LocalDate): Flow<List<AnalyticsDataPoint>>

    @Query("SELECT strftime('%Y-%m-%d', date('1970-01-01', '+' || date || ' days')) as interval, category, SUM(amount) as totalAmount FROM transactions WHERE category != 'CC Payment' AND date BETWEEN :start AND :end GROUP BY interval, category ORDER BY interval DESC")
    fun getCategoryAggregation(start: LocalDate, end: LocalDate): Flow<List<CategoryAggregation>>

    @Query("SELECT * FROM transactions WHERE category != 'CC Payment' AND date BETWEEN :start AND :end ORDER BY date DESC")
    fun getTransactionsInRange(start: LocalDate, end: LocalDate): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM income WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getIncomeInRange(start: LocalDate, end: LocalDate): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM transactions WHERE category != 'CC Payment' ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM income ORDER BY date DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>
}
