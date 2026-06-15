package com.karlvcrisostomo.financialmatrix.domain.usecase

import com.karlvcrisostomo.financialmatrix.features.analytics.data.CategoryAggregation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal

class GetCategorySpendingUseCase {
    
    fun execute(
        aggregationFlow: Flow<List<CategoryAggregation>>,
        selectedCategories: Set<String>
    ): Flow<Map<String, List<CategorySpending>>> {
        return aggregationFlow.map { list -> executeSync(list, selectedCategories) }
    }

    /**
     * Filters aggregated category data based on user selection.
     * Ensures strict exclusion of 'CC Payment' is maintained by upstream DAO,
     * but provides an additional safety layer here.
     */
    fun executeSync(
        list: List<CategoryAggregation>,
        selectedCategories: Set<String>
    ): Map<String, List<CategorySpending>> {
        return list.filter { it.category != "CC Payment" && (selectedCategories.isEmpty() || it.category in selectedCategories) }
            .groupBy { it.interval }
            .mapValues { (_, values) ->
                values.map { CategorySpending(it.category, it.totalAmount) }
            }
    }
}

data class CategorySpending(
    val category: String,
    val amount: BigDecimal
)
