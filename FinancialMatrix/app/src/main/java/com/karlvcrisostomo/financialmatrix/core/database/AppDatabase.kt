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

@Database(entities = [TransactionEntity::class, CreditCardEntity::class, IncomeEntity::class, RecurringTransactionEntity::class], version = 6, exportSchema = true)
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

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Migrate transactions table (Add targetCreditCardId, Change amount to TEXT)
                db.execSQL("CREATE TABLE `transactions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `description` TEXT NOT NULL, `amount` TEXT NOT NULL, `date` INTEGER NOT NULL, `category` TEXT NOT NULL, `isCreditCard` INTEGER NOT NULL, `accountName` TEXT NOT NULL, `targetCreditCardId` INTEGER)")
                db.execSQL("INSERT INTO `transactions_new` (`id`, `description`, `amount`, `date`, `category`, `isCreditCard`, `accountName`, `targetCreditCardId`) SELECT `id`, `description`, CAST(`amount` AS TEXT), `date`, `category`, `isCreditCard`, `accountName`, NULL FROM `transactions`")
                db.execSQL("DROP TABLE `transactions`")
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")

                // 2. Migrate credit_cards table (Change creditLimit to TEXT, Add balance TEXT)
                db.execSQL("CREATE TABLE `credit_cards_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `billingDay` INTEGER NOT NULL, `daysAfterBillingDate` INTEGER NOT NULL, `creditLimit` TEXT NOT NULL, `balance` TEXT NOT NULL)")
                db.execSQL("INSERT INTO `credit_cards_new` (`id`, `name`, `billingDay`, `daysAfterBillingDate`, `creditLimit`, `balance`) SELECT `id`, `name`, `billingDay`, `daysAfterBillingDate`, CAST(`creditLimit` AS TEXT), '0' FROM `credit_cards`")
                
                // Initialize balance for each card based on existing transactions
                db.execSQL("UPDATE `credit_cards_new` SET `balance` = IFNULL((SELECT SUM(amount) FROM `transactions` WHERE `accountName` = `credit_cards_new`.`name` AND `isCreditCard` = 1), '0')")

                db.execSQL("DROP TABLE `credit_cards`")
                db.execSQL("ALTER TABLE `credit_cards_new` RENAME TO `credit_cards`")

                // 3. Migrate income table (Change amount to TEXT)
                db.execSQL("CREATE TABLE `income_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `description` TEXT NOT NULL, `amount` TEXT NOT NULL, `date` INTEGER NOT NULL)")
                db.execSQL("INSERT INTO `income_new` (`id`, `description`, `amount`, `date`) SELECT `id`, `description`, CAST(`amount` AS TEXT), `date` FROM `income`")
                db.execSQL("DROP TABLE `income`")
                db.execSQL("ALTER TABLE `income_new` RENAME TO `income`")

                // 4. Migrate recurring_transactions table (Change amount to TEXT)
                db.execSQL("CREATE TABLE `recurring_transactions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `description` TEXT NOT NULL, `amount` TEXT NOT NULL, `category` TEXT NOT NULL, `isCreditCard` INTEGER NOT NULL, `accountName` TEXT NOT NULL, `frequency` TEXT NOT NULL, `startDate` INTEGER NOT NULL, `nextOccurrence` INTEGER NOT NULL)")
                db.execSQL("INSERT INTO `recurring_transactions_new` (`id`, `description`, `amount`, `category`, `isCreditCard`, `accountName`, `frequency`, `startDate`, `nextOccurrence`) SELECT `id`, `description`, CAST(`amount` AS TEXT), `category`, `isCreditCard`, `accountName`, `frequency`, `startDate`, `nextOccurrence` FROM `recurring_transactions`")
                db.execSQL("DROP TABLE `recurring_transactions`")
                db.execSQL("ALTER TABLE `recurring_transactions_new` RENAME TO `recurring_transactions`")
            }
        }
    }
}