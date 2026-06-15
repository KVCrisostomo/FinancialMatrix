package com.karlvcrisostomo.financialmatrix.features.analytics.ui

import androidx.compose.ui.graphics.Color
import com.karlvcrisostomo.financialmatrix.ui.theme.AlertRed
import com.karlvcrisostomo.financialmatrix.ui.theme.SuccessGreen
import com.karlvcrisostomo.financialmatrix.ui.theme.WarningOrange

object CategoryColorPicker {
    private val staticColors = mapOf(
        "Food" to AlertRed,
        "Utilities" to WarningOrange,
        "Transport" to Color(0xFF355CC0), // Navy/Blue
        "Entertainment" to Color(0xFFC035A0), // Purple/Pink
        "Income" to SuccessGreen,
        "Other" to Color.Gray
    )

    private val dynamicColors = mutableMapOf<String, Color>()
    private val extraColors = listOf(
        Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
        Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF00BCD4),
        Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A)
    )

    fun getColor(category: String): Color {
        return staticColors[category] ?: dynamicColors.getOrPut(category) {
            extraColors[dynamicColors.size % extraColors.size]
        }
    }
}
