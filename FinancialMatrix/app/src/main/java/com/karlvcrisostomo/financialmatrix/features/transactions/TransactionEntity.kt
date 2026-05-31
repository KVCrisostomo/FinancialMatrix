package com.karlvcrisostomo.financialmatrix.features.transactions

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amount: Double,
    val date: LocalDate,
    val category: String,
    val isCreditCard: Boolean,
    val accountName: String
)