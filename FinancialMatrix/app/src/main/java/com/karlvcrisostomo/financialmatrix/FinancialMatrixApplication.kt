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
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardRepository
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.OfflineCreditCardRepository
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeRepository
import com.karlvcrisostomo.financialmatrix.features.income.data.OfflineIncomeRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.OfflineRecurringTransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.OfflineTransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringTransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.worker.RecurringTransactionWorker
import java.util.concurrent.TimeUnit

private const val USER_PREFERENCES_NAME = "user_preferences"
private val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

class FinancialMatrixApplication : Application() {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "financial_matrix_database",
        )
            .addMigrations(AppDatabase.MIGRATION_3_4)
            .build()
    }

    val transactionRepository: TransactionRepository by lazy {
        OfflineTransactionRepository(database.transactionDao())
    }

    val creditCardRepository: CreditCardRepository by lazy {
        OfflineCreditCardRepository(database.creditCardDao())
    }

    val incomeRepository: IncomeRepository by lazy {
        OfflineIncomeRepository(database.incomeDao())
    }

    val recurringTransactionRepository: RecurringTransactionRepository by lazy {
        OfflineRecurringTransactionRepository(database.recurringTransactionDao())
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(dataStore)
    }

    override fun onCreate() {
        super.onCreate()
        setupBudgetMonitor()
        setupRecurringTransactions()
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

    private fun setupRecurringTransactions() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "RecurringTransactionWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}