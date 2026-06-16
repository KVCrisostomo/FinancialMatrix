package com.karlvcrisostomo.financialmatrix.features.transactions.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE category != 'CC Payment' AND strftime('%m', date('1970-01-01', '+' || date || ' days')) = strftime('%m', 'now') AND strftime('%Y', date('1970-01-01', '+' || date || ' days')) = strftime('%Y', 'now')")
    fun getMonthlyTotalSpent(): Flow<BigDecimal?>

    @Query("SELECT SUM(amount) FROM transactions WHERE category != 'CC Payment' AND isCreditCard = 0 AND strftime('%m', date('1970-01-01', '+' || date || ' days')) = strftime('%m', 'now') AND strftime('%Y', date('1970-01-01', '+' || date || ' days')) = strftime('%Y', 'now')")
    fun getMonthlyCashSpent(): Flow<BigDecimal?>

    @Query("SELECT SUM(amount) FROM transactions WHERE category != 'CC Payment' AND isCreditCard = 1 AND strftime('%m', date('1970-01-01', '+' || date || ' days')) = strftime('%m', 'now') AND strftime('%Y', date('1970-01-01', '+' || date || ' days')) = strftime('%Y', 'now')")
    fun getMonthlyCreditSpent(): Flow<BigDecimal?>

    @Query("SELECT category, SUM(amount) as totalAmount FROM transactions WHERE category != 'CC Payment' AND strftime('%m', date('1970-01-01', '+' || date || ' days')) = strftime('%m', 'now') AND strftime('%Y', date('1970-01-01', '+' || date || ' days')) = strftime('%Y', 'now') GROUP BY category")
    fun getMonthlyCategoryAmounts(): Flow<Map<@MapColumn("category") String, @MapColumn("totalAmount") BigDecimal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM credit_cards WHERE id = :cardId")
    suspend fun getCreditCardById(cardId: Long): CreditCardEntity?

    @Query("SELECT * FROM credit_cards WHERE name = :cardName")
    suspend fun getCreditCardByName(cardName: String): CreditCardEntity?

    @Query("UPDATE credit_cards SET balance = :newBalance WHERE id = :cardId")
    suspend fun updateCreditCardBalance(cardId: Long, newBalance: BigDecimal)

    @Transaction
    suspend fun insertCreditCardPayment(transaction: TransactionEntity, targetCardId: Long) {
        // 1. Insert the payment transaction log
        insertTransaction(transaction)

        // 2. Fetch the target card
        val card = getCreditCardById(targetCardId) ?: return

        // 3. Update the card's balance (Payment reduces the balance)
        val newBalance = card.balance.subtract(transaction.amount)
        updateCreditCardBalance(targetCardId, newBalance)
    }

    @Transaction
    suspend fun insertExpenseWithBalanceUpdate(transaction: TransactionEntity, cardId: Long) {
        // 1. Insert the expense transaction log
        insertTransaction(transaction)

        // 2. Fetch the source card
        val card = getCreditCardById(cardId) ?: return

        // 3. Update the card's balance (Expense increases the balance/liability)
        val newBalance = card.balance.add(transaction.amount)
        updateCreditCardBalance(card.id, newBalance)
    }

    @Transaction
    suspend fun deleteTransactionWithBalanceUpdate(transaction: TransactionEntity) {
        // 1. Fetch the associated credit card if targetCreditCardId is present
        val cardId = transaction.targetCreditCardId ?: transaction.id.let { id -> 
            // Fallback for older transactions that might not have targetCreditCardId set yet
            // but are marked as credit card transactions
            if (transaction.isCreditCard) {
                getCreditCardByName(transaction.accountName)?.id
            } else null
        }

        if (cardId != null) {
            val card = getCreditCardById(cardId)
            if (card != null) {
                val category = com.karlvcrisostomo.financialmatrix.domain.model.TransactionCategory.from(transaction.category)
                val newBalance = when {
                    // Deleting a payment increases the debt/balance
                    category is com.karlvcrisostomo.financialmatrix.domain.model.TransactionCategory.CreditCardPayment -> {
                        card.balance.add(transaction.amount)
                    }
                    // Deleting an expense reduces the debt/balance
                    transaction.isCreditCard -> {
                        card.balance.subtract(transaction.amount)
                    }
                    else -> card.balance
                }
                updateCreditCardBalance(card.id, newBalance)
            }
        }

        // 2. Delete the transaction record
        deleteTransaction(transaction)
    }
}