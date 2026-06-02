package com.karlvcrisostomo.financialmatrix.features.transactions.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val amount: Double,
    val category: String,
    val isCreditCard: Boolean,
    val accountName: String,
    val frequency: RecurringFrequency,
    val startDate: LocalDate,
    val nextOccurrence: LocalDate
)
