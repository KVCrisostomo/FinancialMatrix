package com.karlvcrisostomo.financialmatrix.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.math.BigDecimal

data class UserPreferences(
    val currencySymbol: String,
    val defaultIsCreditCard: Boolean,
    val monthlyBudgetLimit: BigDecimal
)

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val DEFAULT_IS_CREDIT_CARD = booleanPreferencesKey("default_is_credit_card")
        val MONTHLY_BUDGET_LIMIT = stringPreferencesKey("monthly_budget_limit")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val currencySymbol = preferences[PreferencesKeys.CURRENCY_SYMBOL] ?: "₱"
            val defaultIsCreditCard = preferences[PreferencesKeys.DEFAULT_IS_CREDIT_CARD] ?: false
            val monthlyBudgetLimitStr = preferences[PreferencesKeys.MONTHLY_BUDGET_LIMIT] ?: "5000.00"
            val monthlyBudgetLimit = try {
                BigDecimal(monthlyBudgetLimitStr)
            } catch (e: Exception) {
                BigDecimal("5000.00")
            }
            UserPreferences(currencySymbol, defaultIsCreditCard, monthlyBudgetLimit)
        }

    suspend fun updateCurrencySymbol(symbol: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_SYMBOL] = symbol
        }
    }

    suspend fun updateDefaultIsCreditCard(isCreditCard: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_IS_CREDIT_CARD] = isCreditCard
        }
    }

    suspend fun updateMonthlyBudgetLimit(limit: BigDecimal) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MONTHLY_BUDGET_LIMIT] = limit.toPlainString()
        }
    }
}
