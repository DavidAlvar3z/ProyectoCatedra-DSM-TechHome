package com.techhome.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchFilter(
    var query: String = "",
    var minPrice: Double = 0.0,
    var maxPrice: Double = 10000.0,
    var category: String = "",
    var sortBy: SortOption = SortOption.RELEVANCE,
    var inStockOnly: Boolean = false,
    var minRating: Float = 0f
) : Parcelable

enum class SortOption {
    RELEVANCE,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    RATING,
    NEWEST
}