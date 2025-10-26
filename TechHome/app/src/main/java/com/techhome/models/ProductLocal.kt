package com.techhome.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ProductLocal(
    val sku: String = "",
    val name: String = "",
    val description: String = "",
    val brand: String = "",
    val model: String = "",
    val image: String = "",
    val url: String = "",
    val regularPrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val stock: Int = 0,
    val category: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0
) {
    // Métodos calculados - NO se guardan en Firestore
    @Exclude
    fun getDiscountPercentage(): Int {
        return if (regularPrice > salePrice && regularPrice > 0) {
            (((regularPrice - salePrice) / regularPrice) * 100).toInt()
        } else {
            0
        }
    }

    @Exclude
    fun getStockStatus(): StockStatus {
        return when {
            stock == 0 -> StockStatus.OUT_OF_STOCK
            stock <= 5 -> StockStatus.LOW_STOCK
            else -> StockStatus.IN_STOCK
        }
    }

    @Exclude
    fun isLowStock(): Boolean = stock in 1..5

    @Exclude
    fun isAvailable(): Boolean = stock > 0
}

enum class StockStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK
}