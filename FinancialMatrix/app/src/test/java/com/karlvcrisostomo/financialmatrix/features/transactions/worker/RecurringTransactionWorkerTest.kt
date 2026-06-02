package com.karlvcrisostomo.financialmatrix.features.transactions.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.karlvcrisostomo.financialmatrix.FinancialMatrixApplication
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringFrequency
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringTransactionEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringTransactionRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class RecurringTransactionWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var recurringRepo: RecurringTransactionRepository
    private lateinit var transRepo: TransactionRepository
    private lateinit var app: FinancialMatrixApplication

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        recurringRepo = mockk(relaxed = true)
        transRepo = mockk(relaxed = true)
        app = mockk(relaxed = true)

        every { context.applicationContext } returns app
        every { app.recurringTransactionRepository } returns recurringRepo
        every { app.transactionRepository } returns transRepo
    }

    @Test
    fun `doWork inserts transaction and updates nextOccurrence`() = runTest {
        val today = LocalDate.now()
        val recurring = RecurringTransactionEntity(
            id = 1,
            description = "Netflix",
            amount = 549.0,
            category = "Entertainment",
            isCreditCard = true,
            accountName = "Visa",
            frequency = RecurringFrequency.MONTHLY,
            startDate = today.minusMonths(1),
            nextOccurrence = today
        )

        coEvery { recurringRepo.getDueRecurringTransactions(today) } returns listOf(recurring)

        val worker = RecurringTransactionWorker(context, workerParams)
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)

        coVerify { transRepo.insertTransaction(any()) }
        coVerify { recurringRepo.updateRecurringTransaction(match { it.nextOccurrence == today.plusMonths(1) }) }
    }
}
