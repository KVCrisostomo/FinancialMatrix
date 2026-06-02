package com.karlvcrisostomo.financialmatrix.features.income.data

import kotlinx.coroutines.flow.Flow

interface IncomeRepository {
    fun getAllIncome(): Flow<List<IncomeEntity>>
    suspend fun insertIncome(income: IncomeEntity)
    suspend fun deleteIncome(income: IncomeEntity)
}

class OfflineIncomeRepository(
    private val incomeDao: IncomeDao
) : IncomeRepository {

    override fun getAllIncome(): Flow<List<IncomeEntity>> =
        incomeDao.getAllIncome()

    override suspend fun insertIncome(income: IncomeEntity) =
        incomeDao.insertIncome(income)

    override suspend fun deleteIncome(income: IncomeEntity) =
        incomeDao.deleteIncome(income)
}
