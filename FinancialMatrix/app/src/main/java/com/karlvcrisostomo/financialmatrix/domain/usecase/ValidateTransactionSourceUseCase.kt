package com.karlvcrisostomo.financialmatrix.domain.usecase

import com.karlvcrisostomo.financialmatrix.domain.model.TransactionCategory

/**
 * Validates that a transaction's category and payment method are compatible.
 * Enforces that a Credit Card Payment cannot be funded by another credit card.
 */
class ValidateTransactionSourceUseCase {
    operator fun invoke(category: String, isCreditCard: Boolean) {
        val transactionCategory = TransactionCategory.from(category)
        
        if (transactionCategory is TransactionCategory.CreditCardPayment && isCreditCard) {
            throw InvalidFundingSourceException("Credit card payments cannot be funded by another credit card.")
        }
    }
}
