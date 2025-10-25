package com.techhome.models

data class CartItem(
    val productId: String = "",
    val name: String = "",
    val image: String = "",
    val salePrice: Double = 0.0,
    val regularPrice: Double = 0.0,
    val quantity: Int = 1,
    val categoryId: String = "",
    val categoryName: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getSubtotal(): Double = salePrice * quantity
}
