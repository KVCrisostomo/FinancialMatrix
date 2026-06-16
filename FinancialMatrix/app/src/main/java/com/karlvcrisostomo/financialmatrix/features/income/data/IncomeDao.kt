package com.karlvcrisostomo.financialmatrix.features.income.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM income ORDER BY date DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>

    @Query("SELECT SUM(amount) FROM income WHERE strftime('%m', date('1970-01-01', '+' || date || ' days')) = strftime('%m', 'now') AND strftime('%Y', date('1970-01-01', '+' || date || ' days')) = strftime('%Y', 'now')")
    fun getMonthlyTotalIncome(): Flow<java.math.BigDecimal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity)

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)
}
