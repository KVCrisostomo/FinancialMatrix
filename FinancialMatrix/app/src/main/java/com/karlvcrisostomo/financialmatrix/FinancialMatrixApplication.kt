package com.karlvcrisostomo.financialmatrix

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.karlvcrisostomo.financialmatrix.core.data.UserPreferencesRepository
import com.karlvcrisostomo.financialmatrix.core.database.AppDatabase
import com.karlvcrisostomo.financialmatrix.features.transactions.data.OfflineTransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository

private const val USER_PREFERENCES_NAME = "user_preferences"
private val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

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

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(dataStore)
    }
}