// models/Favorite.kt
package com.techhome.models

data class Favorite(
    val favoriteId: String = "",
    val userId: String = "",
    val productSku: String = "",
    val productName: String = "",
    val productImage: String = "",
    val price: Double = 0.0,
    val addedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", 0.0, 0L)
}