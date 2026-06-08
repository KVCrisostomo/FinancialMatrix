package com.karlvcrisostomo.financialmatrix

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.AboutScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.karlvcrisostomo.financialmatrix.ui.theme.FinancialMatrixTheme

@RunWith(AndroidJUnit4::class)
class AboutScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aboutScreen_displaysInfoCorrectly() {
        var ossClicked = false
        
        composeTestRule.setContent {
            FinancialMatrixTheme {
                AboutScreen(
                    onNavigateBack = {},
                    onNavigateToOssLicenses = { ossClicked = true }
                )
            }
        }

        // Check if Title is displayed
        composeTestRule.onNodeWithText("Financial Matrix Ledger").assertIsDisplayed()
        
        // Check if Version is displayed (using substring since version might change)
        composeTestRule.onNodeWithText("Version", substring = true).assertIsDisplayed()
        
        // Check if Description is displayed
        composeTestRule.onNodeWithText("Financial Matrix Ledger (FML)", substring = true).assertIsDisplayed()

        // Check and Click Licenses Item
        composeTestRule.onNodeWithText("Open Source Licenses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Open Source Licenses").performClick()
        
        assert(ossClicked)
    }

    @Test
    fun aboutScreen_backButton_triggersCallback() {
        var backClicked = false
        
        composeTestRule.setContent {
            FinancialMatrixTheme {
                AboutScreen(
                    onNavigateBack = { backClicked = true },
                    onNavigateToOssLicenses = {}
                )
            }
        }

        // Click back button
        composeTestRule.onNodeWithContentDescription("Navigate Back").performClick()
        
        assert(backClicked)
    }
}
