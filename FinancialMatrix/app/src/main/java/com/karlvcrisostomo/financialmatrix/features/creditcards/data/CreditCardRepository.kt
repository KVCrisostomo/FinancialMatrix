package com.karlvcrisostomo.financialmatrix.features.creditcards.data

import kotlinx.coroutines.flow.Flow

interface CreditCardRepository {
    fun getAllCards(): Flow<List<CreditCardEntity>>
    suspend fun insertCard(card: CreditCardEntity)
    suspend fun deleteCard(card: CreditCardEntity)
}

class OfflineCreditCardRepository(
    private val creditCardDao: CreditCardDao
) : CreditCardRepository {

    override fun getAllCards(): Flow<List<CreditCardEntity>> =
        creditCardDao.getAllCards()

    override suspend fun insertCard(card: CreditCardEntity) =
        creditCardDao.insertCard(card)

    override suspend fun deleteCard(card: CreditCardEntity) =
        creditCardDao.deleteCard(card)
}
