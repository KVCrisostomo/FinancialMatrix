package com.karlvcrisostomo.financialmatrix.core.util

import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.time.LocalDate

class ExportUtilsTest {

    @Test
    fun `toCsvString returns correct format for empty list`() {
        // Arrange
        val transactions = emptyList<TransactionEntity>()
        val expected = "Date,Description,Amount,Category,PaymentMethod\n"

        // Act
        val result = transactions.toCsvString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `toCsvString returns correct format for multiple transactions`() {
        // Arrange
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                description = "Lunch",
                amount = 250.0,
                date = LocalDate.of(2026, 6, 1),
                category = "Food",
                isCreditCard = false,
                accountName = "Primary"
            ),
            TransactionEntity(
                id = 2,
                description = "Gas",
                amount = 1500.0,
                date = LocalDate.of(2026, 6, 1),
                category = "Transport",
                isCreditCard = true,
                accountName = "Primary"
            )
        )
        val expected = "Date,Description,Amount,Category,PaymentMethod\n" +
                "2026-06-01,\"Lunch\",250.0,Food,Cash\n" +
                "2026-06-01,\"Gas\",1500.0,Transport,Credit"

        // Act
        val result = transactions.toCsvString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `toCsvString handles descriptions with commas correctly`() {
        // Arrange
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                description = "Coffee, snacks",
                amount = 300.0,
                date = LocalDate.of(2026, 6, 1),
                category = "Food",
                isCreditCard = false,
                accountName = "Primary"
            )
        )
        val expected = "Date,Description,Amount,Category,PaymentMethod\n" +
                "2026-06-01,\"Coffee, snacks\",300.0,Food,Cash"

        // Act
        val result = transactions.toCsvString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `exportTransactionsToStream writes correct CSV to stream`() {
        // Arrange
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                description = "Lunch",
                amount = 250.0,
                date = LocalDate.of(2026, 6, 1),
                category = "Food",
                isCreditCard = false,
                accountName = "Primary"
            )
        )
        val expected = "Date,Description,Amount,Category,PaymentMethod\n" +
                "2026-06-01,\"Lunch\",250.0,Food,Cash"
        val outputStream = ByteArrayOutputStream()

        // Act
        exportTransactionsToStream(outputStream, transactions)
        val result = outputStream.toString()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `exportTransactionsToStream writes only header for empty list`() {
        // Arrange
        val transactions = emptyList<TransactionEntity>()
        val expected = "Date,Description,Amount,Category,PaymentMethod\n"
        val outputStream = ByteArrayOutputStream()

        // Act
        exportTransactionsToStream(outputStream, transactions)
        val result = outputStream.toString()

        // Assert
        assertEquals(expected, result)
    }
}
