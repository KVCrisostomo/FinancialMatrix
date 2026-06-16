package com.karlvcrisostomo.financialmatrix.features.transactions.data

import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    fun getMonthlyTotalSpent(): Flow<BigDecimal?>
    fun getMonthlyCashSpent(): Flow<BigDecimal?>
    fun getMonthlyCreditSpent(): Flow<BigDecimal?>
    fun getMonthlyCategoryAmounts(): Flow<Map<String, BigDecimal>>
    suspend fun insertTransaction(transaction: TransactionEntity)
    suspend fun insertCreditCardPayment(transaction: TransactionEntity, targetCardId: Long)
    suspend fun insertExpenseWithBalanceUpdate(transaction: TransactionEntity, cardId: Long)
    suspend fun deleteTransaction(transaction: TransactionEntity)
    suspend fun deleteTransactionWithBalanceUpdate(transaction: TransactionEntity)
}

class OfflineTransactionRepository(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    override fun getMonthlyTotalSpent(): Flow<BigDecimal?> =
        transactionDao.getMonthlyTotalSpent()

    override fun getMonthlyCashSpent(): Flow<BigDecimal?> =
        transactionDao.getMonthlyCashSpent()

    override fun getMonthlyCreditSpent(): Flow<BigDecimal?> =
        transactionDao.getMonthlyCreditSpent()

    override fun getMonthlyCategoryAmounts(): Flow<Map<String, BigDecimal>> =
        transactionDao.getMonthlyCategoryAmounts()

    override suspend fun insertTransaction(transaction: TransactionEntity) =
        transactionDao.insertTransaction(transaction)

    override suspend fun insertCreditCardPayment(transaction: TransactionEntity, targetCardId: Long) =
        transactionDao.insertCreditCardPayment(transaction, targetCardId)

    override suspend fun insertExpenseWithBalanceUpdate(transaction: TransactionEntity, cardId: Long) =
        transactionDao.insertExpenseWithBalanceUpdate(transaction, cardId)

    override suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    override suspend fun deleteTransactionWithBalanceUpdate(transaction: TransactionEntity) =
        transactionDao.deleteTransactionWithBalanceUpdate(transaction)
}