package com.karlvcrisostomo.financialmatrix.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardDao
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardEntity
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeDao
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringTransactionDao
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringTransactionEntity
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionDao
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity

@Database(entities = [TransactionEntity::class, CreditCardEntity::class, IncomeEntity::class, RecurringTransactionEntity::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun incomeDao(): IncomeDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create the new table with the updated schema
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `credit_cards_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `billingDay` INTEGER NOT NULL, 
                        `daysAfterBillingDate` INTEGER NOT NULL, 
                        `creditLimit` REAL NOT NULL
                    )
                    """.trimIndent()
                )

                // 2. Copy data from the old table to the new table
                // We'll set a default of 20 days for daysAfterBillingDate during migration
                db.execSQL(
                    """
                    INSERT INTO `credit_cards_new` (`id`, `name`, `billingDay`, `daysAfterBillingDate`, `creditLimit`)
                    SELECT `id`, `name`, `billingDay`, 20, `creditLimit` FROM `credit_cards`
                    """.trimIndent()
                )

                // 3. Drop the old table
                db.execSQL("DROP TABLE `credit_cards`")

                // 4. Rename the new table to the old table name
                db.execSQL("ALTER TABLE `credit_cards_new` RENAME TO `credit_cards`")
            }
        }
    }
}