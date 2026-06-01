package com.karlvcrisostomo.financialmatrix.features.creditcards.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val billingDay: Int, // 1-31
    val dueDay: Int, // 1-31
    val creditLimit: Double
)
