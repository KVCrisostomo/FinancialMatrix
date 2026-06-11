package com.karlvcrisostomo.financialmatrix.domain.usecase

/**
 * Thrown when a transaction's funding source is invalid 
 * (e.g., paying a credit card with another credit card).
 */
class InvalidFundingSourceException(message: String) : Exception(message)
