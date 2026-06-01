package com.karlvcrisostomo.financialmatrix.core.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun `formatToHumanReadable returns correct format for standard date`() {
        // Arrange
        val date = LocalDate.of(2026, 6, 1)
        val expected = "June 1, 2026"

        // Act
        val result = date.formatToHumanReadable()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `formatToHumanReadable returns correct format for month with multiple digits`() {
        // Arrange
        val date = LocalDate.of(2026, 12, 25)
        val expected = "December 25, 2026"

        // Act
        val result = date.formatToHumanReadable()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `formatToHumanReadable returns correct format for leap year date`() {
        // Arrange
        val date = LocalDate.of(2024, 2, 29)
        val expected = "February 29, 2024"

        // Act
        val result = date.formatToHumanReadable()

        // Assert
        assertEquals(expected, result)
    }
}
