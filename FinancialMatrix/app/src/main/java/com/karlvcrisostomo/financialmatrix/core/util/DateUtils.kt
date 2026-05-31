package com.karlvcrisostomo.financialmatrix.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Formats a [LocalDate] into a human-readable string using the US locale.
 * Example: 2026-06-01 -> June 1, 2026
 */
fun LocalDate.formatToHumanReadable(): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.US)
    return this.format(formatter)
}
