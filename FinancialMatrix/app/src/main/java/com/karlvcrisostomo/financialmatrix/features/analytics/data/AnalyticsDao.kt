package com.karlvcrisostomo.financialmatrix.features.analytics.data

import androidx.room.Dao
import androidx.room.Query
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AnalyticsDao {

    @Query("SELECT * FROM transactions WHERE category != 'CC Payment' AND date BETWEEN :start AND :end ORDER BY date DESC")
    fun getTransactionsInRange(start: LocalDate, end: LocalDate): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM income WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getIncomeInRange(start: LocalDate, end: LocalDate): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM transactions WHERE category != 'CC Payment' ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM income ORDER BY date DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>
}
