package com.karlvcrisostomo.financialmatrix.features.analytics.data

import androidx.room.Dao
import androidx.room.Query
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {

    @Query("SELECT * FROM transactions WHERE category != 'CC Payment' ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM income ORDER BY date DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>
}
