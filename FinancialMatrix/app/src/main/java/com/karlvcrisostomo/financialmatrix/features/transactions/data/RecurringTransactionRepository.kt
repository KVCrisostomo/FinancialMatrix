package com.karlvcrisostomo.financialmatrix.features.transactions.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RecurringTransactionRepository {
    fun getAllRecurringTransactions(): Flow<List<RecurringTransactionEntity>>
    suspend fun getDueRecurringTransactions(date: LocalDate): List<RecurringTransactionEntity>
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransactionEntity)
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransactionEntity)
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransactionEntity)
}

class OfflineRecurringTransactionRepository(
    private val recurringTransactionDao: RecurringTransactionDao
) : RecurringTransactionRepository {
    override fun getAllRecurringTransactions(): Flow<List<RecurringTransactionEntity>> =
        recurringTransactionDao.getAllRecurringTransactions()

    override suspend fun getDueRecurringTransactions(date: LocalDate): List<RecurringTransactionEntity> =
        recurringTransactionDao.getDueRecurringTransactions(date)

    override suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransactionEntity) =
        recurringTransactionDao.insertRecurringTransaction(recurringTransaction)

    override suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransactionEntity) =
        recurringTransactionDao.updateRecurringTransaction(recurringTransaction)

    override suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransactionEntity) =
        recurringTransactionDao.deleteRecurringTransaction(recurringTransaction)
}
