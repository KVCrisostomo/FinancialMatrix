package com.karlvcrisostomo.financialmatrix.features.creditcards.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards ORDER BY name ASC")
    fun getAllCards(): Flow<List<CreditCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CreditCardEntity)

    @Delete
    suspend fun deleteCard(card: CreditCardEntity)
}
