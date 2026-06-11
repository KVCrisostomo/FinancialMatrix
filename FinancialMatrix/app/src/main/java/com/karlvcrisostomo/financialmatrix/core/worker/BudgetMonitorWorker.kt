package com.karlvcrisostomo.financialmatrix.core.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.karlvcrisostomo.financialmatrix.FinancialMatrixApplication
import com.karlvcrisostomo.financialmatrix.core.util.NotificationHelper
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
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
            .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }

        val preferences = preferencesRepository.userPreferencesFlow.first()
        val limit = BigDecimal(preferences.monthlyBudgetLimit.toString())
        val currency = preferences.currencySymbol

        if (limit > BigDecimal.ZERO) {
            val percentage = monthlyTotal.divide(limit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
            
            when {
                percentage >= BigDecimal("100") -> {
                    notificationHelper.showBudgetAlert(
                        "Budget Exceeded!",
                        "You have spent $currency${String.format(Locale.US, "%.2f", monthlyTotal)}, which is over your $currency${String.format(Locale.US, "%.2f", limit)} limit."
                    )
                }
                percentage >= BigDecimal("80") -> {
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