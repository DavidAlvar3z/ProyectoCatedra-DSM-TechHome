package com.techhome.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ProductLocal(
    @DocumentId
    val id: String = "",
    val sku: String = "",
    val name: String = "",
    val description: String = "",
    val salePrice: Double = 0.0,
    val regularPrice: Double = 0.0,
    val image: String = "",
    val url: String = "",
    val categoryId: String = "",
    val categoryName: String = "",

    // Campos de inventario
    val stock: Int = 0,
    val lowStockThreshold: Int = 5,
    val isAvailable: Boolean = true,

    // Metadata
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null,
    val lastSyncedFromBestBuy: Long = 0,

    // Características adicionales
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val brand: String = "",
    val model: String = ""
) {
    // Helper para verificar si está en stock bajo
    fun isLowStock(): Boolean = stock in 1..lowStockThreshold

    // Helper para calcular descuento
    fun getDiscountPercentage(): Int {
        if (regularPrice <= salePrice) return 0
        return ((1 - (salePrice / regularPrice)) * 100).toInt()
    }

    // Helper para el estado del stock
    fun getStockStatus(): StockStatus {
        return when {
            stock == 0 -> StockStatus.OUT_OF_STOCK
            stock <= lowStockThreshold -> StockStatus.LOW_STOCK
            else -> StockStatus.IN_STOCK
        }
    }
}

enum class StockStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK
}