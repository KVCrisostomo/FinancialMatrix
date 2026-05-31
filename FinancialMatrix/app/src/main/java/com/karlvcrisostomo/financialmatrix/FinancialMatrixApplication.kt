package com.karlvcrisostomo.financialmatrix

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.karlvcrisostomo.financialmatrix.core.data.UserPreferencesRepository
import com.karlvcrisostomo.financialmatrix.core.database.AppDatabase
import com.karlvcrisostomo.financialmatrix.core.worker.BudgetMonitorWorker
import java.util.concurrent.TimeUnit
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

    override fun onCreate() {
        super.onCreate()
        setupBudgetMonitor()
    }

    private fun setupBudgetMonitor() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<BudgetMonitorWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BudgetMonitorWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}