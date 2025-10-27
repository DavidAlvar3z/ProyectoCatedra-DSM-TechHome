package com.techhome.models

enum class OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val shippingAddress: ShippingAddress? = null,
    val paymentMethod: PaymentMethod? = null,
    val subtotal: Double = 0.0,
    val shippingCost: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getItemsCount(): Int {
        return items.sumOf { it.quantity }
    }

    fun getStatusText(): String {
        return when (status) {
            OrderStatus.PENDING -> "Pendiente"
            OrderStatus.PROCESSING -> "En proceso"
            OrderStatus.SHIPPED -> "Enviado"
            OrderStatus.DELIVERED -> "Entregado"
            OrderStatus.CANCELLED -> "Cancelado"
        }
    }
}