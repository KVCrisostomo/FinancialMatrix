package com.karlvcrisostomo.financialmatrix.features.creditcards.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.karlvcrisostomo.financialmatrix.core.util.formatToHumanReadable
import com.karlvcrisostomo.financialmatrix.domain.util.StatementCycleCalculator
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardEntity
import java.util.Locale

@Composable
fun CreditCardDashboard(
    uiState: CreditCardUiState,
    currencySymbol: String,
    onAddCardClick: () -> Unit,
    onEditCardClick: (CreditCardEntity) -> Unit,
    onDeleteCardClick: (CreditCardEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Credit Card Monitoring",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (uiState.cards.isEmpty()) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onAddCardClick() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add Your First Credit Card",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.cards) { stats ->
                    CreditCardSummaryCard(
                        stats = stats, 
                        currencySymbol = currencySymbol,
                        onEdit = { onEditCardClick(stats.card) },
                        onDelete = { onDeleteCardClick(stats.card) }
                    )
                }
            }
        }
    }
}

@Composable
fun CreditCardSummaryCard(
    stats: CreditCardStats,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dueDate = StatementCycleCalculator.calculateDueDate(
        stats.statementWindowEnd,
        stats.card.daysAfterBillingDate
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stats.card.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = "Due: ${dueDate.formatToHumanReadable()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = "Edit Card",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Delete Card",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = "Statement Balance", style = MaterialTheme.typography.labelSmall)
            Text(
                text = "$currencySymbol${String.format(Locale.US, "%.2f", stats.statementBalance)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "${stats.statementWindowStart.formatToHumanReadable()} - ${stats.statementWindowEnd.formatToHumanReadable()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Current Balance", style = MaterialTheme.typography.labelSmall)
                    Text(text = "$currencySymbol${String.format(Locale.US, "%.2f", stats.currentBalance)}", style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Remaining", style = MaterialTheme.typography.labelSmall)
                    Text(text = "$currencySymbol${String.format(Locale.US, "%.2f", stats.remainingLimit)}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            LinearProgressIndicator(
                progress = { (stats.utilizationPercentage.divide(java.math.BigDecimal("100"), 4, java.math.RoundingMode.HALF_EVEN)).coerceIn(java.math.BigDecimal.ZERO, java.math.BigDecimal.ONE).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(6.dp),
                color = if (stats.utilizationPercentage > java.math.BigDecimal("80")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
