package com.KVCrisostomo.financialmatrix

import android.app.Application
import androidx.room.Room
import com.karlvcrisostomo.financialmatrix.core.database.AppDatabase
import com.karlvcrisostomo.financialmatrix.features.transactions.data.OfflineTransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository

class FinancialMatrixApplication : Application() {

    // Thread-safe singleton instantiation of the database instance
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "financial_matrix_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // Expose the feature repository globally to be consumed by ViewModels
    val transactionRepository: TransactionRepository by lazy {
        OfflineTransactionRepository(database.transactionDao())
    }
}