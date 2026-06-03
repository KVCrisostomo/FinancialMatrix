package com.karlvcrisostomo.financialmatrix.domain.model

sealed class TransactionCategory(val displayName: String) {
    data object Food : TransactionCategory("Food")
    data object Utilities : TransactionCategory("Utilities")
    data object Transport : TransactionCategory("Transport")
    data object Entertainment : TransactionCategory("Entertainment")
    data object CreditCardPayment : TransactionCategory("CC Payment")
    data object Other : TransactionCategory("Other")
    data class Custom(val name: String) : TransactionCategory(name)

    /**
     * Determines if the category represents an internal liability transfer 
     * (e.g., paying off a credit card) which should be excluded from spending KPIs.
     */
    fun isInternalTransfer(): Boolean = this is CreditCardPayment

    companion object {
        fun from(name: String): TransactionCategory {
            return when (name) {
                Food.displayName -> Food
                Utilities.displayName -> Utilities
                Transport.displayName -> Transport
                Entertainment.displayName -> Entertainment
                CreditCardPayment.displayName -> CreditCardPayment
                Other.displayName -> Other
                else -> Custom(name)
            }
        }

        fun getAllStandard(): List<TransactionCategory> {
            return listOf(Food, Utilities, Transport, Entertainment, CreditCardPayment, Other)
        }
    }
}
