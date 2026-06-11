package com.karlvcrisostomo.financialmatrix.features.creditcards.data

import androidx.room.Entity
import androidx.room.PrimaryKey

import java.math.BigDecimal

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val billingDay: Int, // 1-31
    val daysAfterBillingDate: Int, // Relative offset (e.g., 20 days)
    val creditLimit: BigDecimal,
    val balance: BigDecimal = BigDecimal.ZERO
)
