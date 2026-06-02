package com.karlvcrisostomo.financialmatrix.domain.model

sealed class TransactionCategory(val displayName: String) {
    object Food : TransactionCategory("Food")
    object Utilities : TransactionCategory("Utilities")
    object Transport : TransactionCategory("Transport")
    object Entertainment : TransactionCategory("Entertainment")
    object CreditCardPayment : TransactionCategory("CC Payment")
    object Other : TransactionCategory("Other")
    data class Custom(val name: String) : TransactionCategory(name)

    fun isInternalTransfer(): Boolean {
        return this is CreditCardPayment
    }

    companion object {
        fun from(name: String): TransactionCategory {
            return when (name) {
                "Food" -> Food
                "Utilities" -> Utilities
                "Transport" -> Transport
                "Entertainment" -> Entertainment
                "CC Payment" -> CreditCardPayment
                "Other" -> Other
                else -> Custom(name)
            }
        }

        fun getAllStandard(): List<TransactionCategory> {
            return listOf(Food, Utilities, Transport, Entertainment, CreditCardPayment, Other)
        }
    }
}
