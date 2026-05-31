package com.karlvcrisostomo.financialmatrix

import android.app.Application
import androidx.room.Room
import com.karlvcrisostomo.financialmatrix.core.database.AppDatabase
import com.karlvcrisostomo.financialmatrix.features.transactions.data.OfflineTransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository

class FinancialMatrixApplication : Application() {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "financial_matrix_database",
        )
            // Explicitly pass true to indicate all tables should be dropped on schema fallback
            .fallbackToDestructiveMigration(true)
            .build()
    }

    val transactionRepository: TransactionRepository by lazy {
        OfflineTransactionRepository(database.transactionDao())
    }
}