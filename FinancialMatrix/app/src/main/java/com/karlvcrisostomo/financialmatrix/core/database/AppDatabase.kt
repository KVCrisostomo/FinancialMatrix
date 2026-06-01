package com.karlvcrisostomo.financialmatrix.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardDao
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionDao
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity

@Database(entities = [TransactionEntity::class, CreditCardEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun creditCardDao(): CreditCardDao
}