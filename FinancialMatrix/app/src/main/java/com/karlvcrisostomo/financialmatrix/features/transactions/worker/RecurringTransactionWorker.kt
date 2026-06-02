package com.karlvcrisostomo.financialmatrix.features.transactions.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.karlvcrisostomo.financialmatrix.FinancialMatrixApplication
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringFrequency
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import java.time.LocalDate

class RecurringTransactionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as FinancialMatrixApplication
        val recurringRepo = app.recurringTransactionRepository
        val transRepo = app.transactionRepository
        
        val today = LocalDate.now()
        val dueTransactions = recurringRepo.getDueRecurringTransactions(today)

        dueTransactions.forEach { recurring ->
            // 1. Insert into ledger
            val newTransaction = TransactionEntity(
                description = recurring.description,
                amount = recurring.amount,
                date = today,
                category = recurring.category,
                isCreditCard = recurring.isCreditCard,
                accountName = recurring.accountName
            )
            transRepo.insertTransaction(newTransaction)

            // 2. Schedule next occurrence
            val nextDate = when (recurring.frequency) {
                RecurringFrequency.DAILY -> recurring.nextOccurrence.plusDays(1)
                RecurringFrequency.WEEKLY -> recurring.nextOccurrence.plusWeeks(1)
                RecurringFrequency.MONTHLY -> recurring.nextOccurrence.plusMonths(1)
            }
            
            recurringRepo.updateRecurringTransaction(
                recurring.copy(nextOccurrence = nextDate)
            )
        }

        return Result.success()
    }
}
