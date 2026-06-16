package com.karlvcrisostomo.financialmatrix.features.analytics.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.karlvcrisostomo.financialmatrix.FinancialMatrixApplication
import com.karlvcrisostomo.financialmatrix.domain.usecase.CategorySpending
import com.karlvcrisostomo.financialmatrix.domain.usecase.GetCategorySpendingUseCase
import com.karlvcrisostomo.financialmatrix.features.analytics.data.AnalyticsDataPoint
import com.karlvcrisostomo.financialmatrix.features.analytics.data.AnalyticsRepository
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AnalyticsTimeline {
    WEEK, MONTH, YEAR
}

data class AnalyticsUiState(
    val timeline: AnalyticsTimeline = AnalyticsTimeline.MONTH,
    val dataPoints: List<AnalyticsDataPoint> = emptyList(),
    val categorySpending: Map<String, List<CategorySpending>> = emptyMap(),
    val availableCategories: List<String> = emptyList(),
    val selectedCategories: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AnalyticsViewModel(
    private val repository: AnalyticsRepository,
    private val getCategorySpendingUseCase: GetCategorySpendingUseCase = GetCategorySpendingUseCase()
) : ViewModel() {

    private val _timeline = MutableStateFlow(AnalyticsTimeline.MONTH)
    val timeline: StateFlow<AnalyticsTimeline> = _timeline.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())

    val entryModelProducer = ChartEntryModelProducer()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AnalyticsUiState> = combine(
        _timeline,
        _selectedCategories,
        _timeline.flatMapLatest { timeline ->
            when (timeline) {
                AnalyticsTimeline.WEEK -> repository.getWeeklyAnalytics()
                AnalyticsTimeline.MONTH -> repository.getMonthlyAnalytics()
                AnalyticsTimeline.YEAR -> repository.getYearlyAnalytics()
            }
        },
        _timeline.flatMapLatest { timeline ->
            when (timeline) {
                AnalyticsTimeline.WEEK -> repository.getWeeklyCategoryAggregation()
                AnalyticsTimeline.MONTH -> repository.getMonthlyCategoryAggregation()
                AnalyticsTimeline.YEAR -> repository.getYearlyCategoryAggregation()
            }
        }
    ) { timeline, selected, baseData, rawCategoryAggregation ->
        val filteredCategoryData = getCategorySpendingUseCase.executeSync(rawCategoryAggregation, selected)
        
        val expenseCategories = rawCategoryAggregation.map { it.category }.distinct().sorted()
        val allAvailableCategories = expenseCategories + listOf("Income")
        
        AnalyticsUiState(
            timeline = timeline,
            dataPoints = baseData,
            categorySpending = filteredCategoryData,
            availableCategories = allAvailableCategories,
            selectedCategories = selected,
            isLoading = false
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState(isLoading = true)
    )

    init {
        uiState.onEach { state ->
            if (!state.isLoading) {
                updateChart(state.dataPoints, state.categorySpending, state.selectedCategories)
            }
        }.launchIn(viewModelScope)
    }

    fun updateTimeline(timeline: AnalyticsTimeline) {
        _timeline.value = timeline
    }

    fun toggleCategory(category: String) {
        val current = _selectedCategories.value
        _selectedCategories.value = if (category in current) {
            current - category
        } else {
            current + category
        }
    }

    private fun updateChart(
        baseData: List<AnalyticsDataPoint>,
        categoryData: Map<String, List<CategorySpending>>,
        selectedCategories: Set<String>
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val reversedBase = baseData.reversed()
                
                // Determine which categories to show (Expenses)
                val expenseCategories = if (selectedCategories.isEmpty()) {
                    categoryData.values.flatten().map { it.category }.distinct().sorted()
                } else {
                    selectedCategories.filter { it != "Income" }.sorted()
                }

                val categorySeries = expenseCategories.map { category ->
                    reversedBase.mapIndexed { index, point ->
                        val amount = categoryData[point.interval]?.find { it.category == category }?.amount ?: java.math.BigDecimal.ZERO
                        entryOf(index.toFloat(), amount.toFloat())
                    }
                }

                // Determine if Income should be shown
                val showIncome = selectedCategories.isEmpty() || "Income" in selectedCategories
                val incomeEntries = if (showIncome) {
                    reversedBase.mapIndexed { index, point ->
                        entryOf(index.toFloat(), point.totalIncome.toFloat())
                    }
                } else {
                    null
                }
                
                // In a stacked chart, Vico stacks the series in order.
                // We'll put category segment series first, then income.
                val allSeries = if (incomeEntries != null) {
                    categorySeries + listOf(incomeEntries)
                } else {
                    categorySeries
                }
                
                if (allSeries.isNotEmpty() && allSeries.all { it.isNotEmpty() }) {
                    entryModelProducer.setEntries(allSeries)
                } else {
                    entryModelProducer.setEntries(emptyList<List<com.patrykandpatrick.vico.core.entry.ChartEntry>>())
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinancialMatrixApplication
                return AnalyticsViewModel(application.analyticsRepository) as T
            }
        }
    }
}
