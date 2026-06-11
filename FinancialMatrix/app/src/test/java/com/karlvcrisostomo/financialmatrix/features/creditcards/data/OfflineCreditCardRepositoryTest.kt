package com.karlvcrisostomo.financialmatrix.features.creditcards.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class OfflineCreditCardRepositoryTest {

    private val creditCardDao: CreditCardDao = mockk()
    private lateinit var repository: OfflineCreditCardRepository

    @Before
    fun setup() {
        repository = OfflineCreditCardRepository(creditCardDao)
    }

    @Test
    fun `getAllCards returns flow from DAO`() = runTest {
        // Arrange
        val expectedCards = listOf(
            CreditCardEntity(1, "Visa Gold", 15, 5, BigDecimal("50000.0"), BigDecimal.ZERO),
            CreditCardEntity(2, "Mastercard Silver", 20, 10, BigDecimal("30000.0"), BigDecimal.ZERO)
        )
        every { creditCardDao.getAllCards() } returns flowOf(expectedCards)

        // Act
        val result = repository.getAllCards()

        // Assert
        result.collect { actualCards ->
            assertEquals(expectedCards, actualCards)
        }
        coVerify(exactly = 1) { creditCardDao.getAllCards() }
    }

    @Test
    fun `insertCard calls DAO insert`() = runTest {
        // Arrange
        val card = CreditCardEntity(1, "Visa Gold", 15, 5, BigDecimal("50000.0"), BigDecimal.ZERO)
        coEvery { creditCardDao.insertCard(card) } returns Unit

        // Act
        repository.insertCard(card)

        // Assert
        coVerify(exactly = 1) { creditCardDao.insertCard(card) }
    }

    @Test
    fun `deleteCard calls DAO delete`() = runTest {
        // Arrange
        val card = CreditCardEntity(1, "Visa Gold", 15, 5, BigDecimal("50000.0"), BigDecimal.ZERO)
        coEvery { creditCardDao.deleteCard(card) } returns Unit

        // Act
        repository.deleteCard(card)

        // Assert
        coVerify(exactly = 1) { creditCardDao.deleteCard(card) }
    }
}