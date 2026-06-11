package com.karlvcrisostomo.financialmatrix.features.transactions.data

import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<TransactionEntity>>
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