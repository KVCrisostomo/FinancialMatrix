package com.karlvcrisostomo.financialmatrix.core.util

import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import java.io.OutputStream

/**
 * Converts a list of [TransactionEntity] into a CSV formatted string.
 */
fun List<TransactionEntity>.toCsvString(): String {
    val header = "Date,Description,Amount,Category,PaymentMethod\n"
    val rows = joinToString(separator = "\n") { transaction ->
        val paymentMethod = if (transaction.isCreditCard) "Credit" else "Cash"
        "${transaction.date},\"${transaction.description}\",${transaction.amount},${transaction.category},$paymentMethod"
    }
    return header + rows
}

/**
 * Writes the transaction list to the given [OutputStream] in CSV format.
 */
fun exportTransactionsToStream(outputStream: OutputStream, transactions: List<TransactionEntity>) {
    outputStream.bufferedWriter().use { writer ->
        writer.write(transactions.toCsvString())
    }
}
