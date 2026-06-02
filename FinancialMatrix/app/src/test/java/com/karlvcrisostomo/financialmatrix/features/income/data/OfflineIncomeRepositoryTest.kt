package com.karlvcrisostomo.financialmatrix.features.income.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class OfflineIncomeRepositoryTest {

    private val incomeDao: IncomeDao = mockk()
    private lateinit var repository: OfflineIncomeRepository

    @Before
    fun setup() {
        repository = OfflineIncomeRepository(incomeDao)
    }

    @Test
    fun `getAllIncome returns flow from DAO`() = runTest {
        // Arrange
        val expectedIncome = listOf(
            IncomeEntity(1, "Salary", 5000.0, LocalDate.now()),
            IncomeEntity(2, "Freelance", 1200.0, LocalDate.now())
        )
        every { incomeDao.getAllIncome() } returns flowOf(expectedIncome)

        // Act
        val result = repository.getAllIncome()

        // Assert
        result.collect { actualIncome ->
            assertEquals(expectedIncome, actualIncome)
        }
        coVerify(exactly = 1) { incomeDao.getAllIncome() }
    }

    @Test
    fun `insertIncome calls DAO insert`() = runTest {
        // Arrange
        val income = IncomeEntity(1, "Salary", 5000.0, LocalDate.now())
        coEvery { incomeDao.insertIncome(income) } returns Unit

        // Act
        repository.insertIncome(income)

        // Assert
        coVerify(exactly = 1) { incomeDao.insertIncome(income) }
    }

    @Test
    fun `deleteIncome calls DAO delete`() = runTest {
        // Arrange
        val income = IncomeEntity(1, "Salary", 5000.0, LocalDate.now())
        coEvery { incomeDao.deleteIncome(income) } returns Unit

        // Act
        repository.deleteIncome(income)

        // Assert
        coVerify(exactly = 1) { incomeDao.deleteIncome(income) }
    }
}
