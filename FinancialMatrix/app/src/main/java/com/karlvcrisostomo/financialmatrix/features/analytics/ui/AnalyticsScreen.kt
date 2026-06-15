package com.karlvcrisostomo.financialmatrix.features.analytics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.karlvcrisostomo.financialmatrix.ui.theme.AlertRed
import com.karlvcrisostomo.financialmatrix.ui.theme.LightGold
import com.karlvcrisostomo.financialmatrix.ui.theme.PremiumGold
import com.karlvcrisostomo.financialmatrix.ui.theme.SuccessGreen
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.axisLineComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TimelineToggle(
            selectedTimeline = uiState.timeline,
            onTimelineSelected = { viewModel.updateTimeline(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            AnalyticsChart(viewModel = viewModel, uiState = uiState)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CategoryFilters(
                categories = uiState.availableCategories,
                selected = uiState.selectedCategories,
                onToggle = { viewModel.toggleCategory(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            ChartLegend(uiState = uiState)
        }
    }
}

@Composable
fun CategoryFilters(
    categories: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selected.isEmpty() || category in selected,
                onClick = { onToggle(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CategoryColorPicker.getColor(category).copy(alpha = 0.2f),
                    selectedLabelColor = CategoryColorPicker.getColor(category),
                    labelColor = LightGold.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
fun TimelineToggle(
    selectedTimeline: AnalyticsTimeline,
    onTimelineSelected: (AnalyticsTimeline) -> Unit
) {
    val options = AnalyticsTimeline.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, timeline ->
            SegmentedButton(
                selected = selectedTimeline == timeline,
                onClick = { onTimelineSelected(timeline) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text(timeline.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
fun AnalyticsChart(
    viewModel: AnalyticsViewModel,
    uiState: AnalyticsUiState
) {
    val dateTimeFormatter = remember(uiState.timeline) {
        when (uiState.timeline) {
            AnalyticsTimeline.WEEK -> DateTimeFormatter.ofPattern("MMM dd")
            AnalyticsTimeline.MONTH -> DateTimeFormatter.ofPattern("MMM")
            AnalyticsTimeline.YEAR -> DateTimeFormatter.ofPattern("yyyy")
        }
    }

    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val dataPoints = uiState.dataPoints.reversed()
        val index = value.toInt()
        if (index in dataPoints.indices) {
            try {
                val date = LocalDate.parse(dataPoints[index].interval)
                date.format(dateTimeFormatter)
            } catch (e: Exception) {
                ""
            }
        } else {
            ""
        }
    }

    // Determine colors based on categories shown (matching ViewModel logic)
    val expenseCategories = if (uiState.selectedCategories.isEmpty()) {
        uiState.availableCategories.filter { it != "Income" }.sorted()
    } else {
        uiState.selectedCategories.filter { it != "Income" }.sorted()
    }
    
    val showIncome = uiState.selectedCategories.isEmpty() || "Income" in uiState.selectedCategories
    
    val chartColors = expenseCategories.map { CategoryColorPicker.getColor(it) }.toMutableList()
    if (showIncome) {
        chartColors.add(SuccessGreen)
    }

    Chart(
        chart = columnChart(
            columns = chartColors.map { lineComponent(color = it, thickness = 8.dp) },
            // Phase 2: Use Stack merge mode to visually reflect segmented data
            mergeMode = ColumnChart.MergeMode.Stack
        ),
        chartModelProducer = viewModel.entryModelProducer,
        startAxis = rememberStartAxis(
            label = axisLabelComponent(color = LightGold),
            axis = lineComponent(color = PremiumGold, thickness = 1.dp)
        ),
        bottomAxis = rememberBottomAxis(
            valueFormatter = bottomAxisValueFormatter,
            label = axisLabelComponent(color = LightGold),
            axis = lineComponent(color = PremiumGold, thickness = 1.dp)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}

@Composable
fun ChartLegend(uiState: AnalyticsUiState) {
    val expenseCategories = if (uiState.selectedCategories.isEmpty()) {
        uiState.availableCategories.filter { it != "Income" }.sorted()
    } else {
        uiState.selectedCategories.filter { it != "Income" }.sorted()
    }

    val showIncome = uiState.selectedCategories.isEmpty() || "Income" in uiState.selectedCategories

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        expenseCategories.forEach { category ->
            LegendItem(color = CategoryColorPicker.getColor(category), label = category)
        }
        if (showIncome) {
            LegendItem(color = SuccessGreen, label = "Income")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = LightGold
            )
        )
    }
}
