package com.techhome.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class CartItem(
    val cartItemId: String = "",
    val productSku: String = "",
    val productName: String = "",
    val productImage: String = "",
    val price: Double = 0.0,
    val regularPrice: Double = 0.0,
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
) {
    @Exclude
    fun getSubtotal(): Double = price * quantity
}