package com.karlvcrisostomo.financialmatrix.core.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.karlvcrisostomo.financialmatrix.FinancialMatrixApplication
import com.karlvcrisostomo.financialmatrix.core.util.NotificationHelper
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.Locale

class BudgetMonitorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as FinancialMatrixApplication
        val repository = application.transactionRepository
        val preferencesRepository = application.userPreferencesRepository
        val notificationHelper = NotificationHelper(applicationContext)

        val transactions = repository.getAllTransactions().first()
        val currentMonth = LocalDate.now().month
        val currentYear = LocalDate.now().year
        
        val monthlyTotal = transactions
            .filter { it.date.month == currentMonth && it.date.year == currentYear }
            .sumOf { it.amount }

        val preferences = preferencesRepository.userPreferencesFlow.first()
        val limit = preferences.monthlyBudgetLimit
        val currency = preferences.currencySymbol

        if (limit > 0) {
            val percentage = (monthlyTotal / limit) * 100
            when {
                percentage >= 100 -> {
                    notificationHelper.showBudgetAlert(
                        "Budget Exceeded!",
                        "You have spent $currency${String.format(Locale.US, "%.2f", monthlyTotal)}, which is over your $currency${String.format(Locale.US, "%.2f", limit)} limit."
                    )
                }
                percentage >= 80 -> {
                    notificationHelper.showBudgetAlert(
                        "Budget Warning",
                        "You have reached ${percentage.toInt()}% of your monthly budget ($currency${String.format(Locale.US, "%.2f", monthlyTotal)} of $currency${String.format(Locale.US, "%.2f", limit)})."
                    )
                }
            }
        }

        return Result.success()
    }
}
