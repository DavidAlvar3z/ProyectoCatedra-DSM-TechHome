package com.techhome.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchFilter(
    var query: String = "",
    var minPrice: Double = 0.0,
    var maxPrice: Double = 3500.0,
    var selectedCategory: String = "",
    var selectedBrand: String = "",
    var minRating: Double = 0.0,
    var inStockOnly: Boolean = false,
    var sortBy: SortOption = SortOption.NAME_ASC
) : Parcelable

enum class SortOption {
    NAME_ASC,
    NAME_DESC,
    PRICE_ASC,
    PRICE_DESC,
    RATING_DESC,
    NEWEST
}