package com.karlvcrisostomo.financialmatrix.core.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Locale

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun fromTimestamp_convertsCorrectly() {
        val epochDay = 19448L // 2023-04-01
        val result = converters.fromTimestamp(epochDay)
        assertEquals(LocalDate.of(2023, 4, 1), result)
    }

    @Test
    fun fromTimestamp_returnsNullForNull() {
        assertNull(converters.fromTimestamp(null))
    }

    @Test
    fun dateToTimestamp_convertsCorrectly() {
        val date = LocalDate.of(2023, 4, 1)
        val result = converters.dateToTimestamp(date)
        assertEquals(19448L, result)
    }

    @Test
    fun dateToTimestamp_returnsNullForNull() {
        assertNull(converters.dateToTimestamp(null))
    }

    @Test
    fun fromBigDecimal_convertsCorrectly() {
        val value = "123.45"
        val result = converters.fromBigDecimal(value)
        assertEquals(BigDecimal("123.45"), result)
    }

    @Test
    fun fromBigDecimal_returnsNullForNull() {
        assertNull(converters.fromBigDecimal(null))
    }

    @Test
    fun bigDecimalToString_convertsCorrectly() {
        val value = BigDecimal("123.45")
        val result = converters.bigDecimalToString(value)
        assertEquals("123.45", result)
    }

    @Test
    fun bigDecimalToString_returnsNullForNull() {
        assertNull(converters.bigDecimalToString(null))
    }

    @Test
    fun bigDecimal_isLocaleIndependent() {
        val originalLocale = Locale.getDefault()
        try {
            // Set a locale that uses ',' as decimal separator
            Locale.setDefault(Locale.GERMANY)
            
            val value = BigDecimal("1234.56")
            val stringValue = converters.bigDecimalToString(value)
            
            // BigDecimal.toString() should always use '.' regardless of locale
            assertEquals("1234.56", stringValue)
            
            val backToBigDecimal = converters.fromBigDecimal(stringValue)
            assertEquals(value, backToBigDecimal)
            
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun bigDecimal_handlesLargePrecision() {
        val value = BigDecimal("123456789012345678.90123456789")
        val stringValue = converters.bigDecimalToString(value)
        val backToBigDecimal = converters.fromBigDecimal(stringValue)
        assertEquals(value, backToBigDecimal)
    }

    @Test
    fun bigDecimal_handlesNegativeValues() {
        val value = BigDecimal("-100.00")
        val stringValue = converters.bigDecimalToString(value)
        assertEquals("-100.00", stringValue)
        val backToBigDecimal = converters.fromBigDecimal(stringValue)
        assertEquals(value, backToBigDecimal)
    }
}
