package com.techhome.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.techhome.models.Order

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    companion object {
        private const val TAG = "OrderRepository"
    }

    fun createOrder(
        order: Order,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        ordersCollection.document(order.orderId)
            .set(order)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Orden creada: ${order.orderId}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al crear orden", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    fun getOrderById(
        orderId: String,
        onSuccess: (Order?) -> Unit,
        onError: (String) -> Unit
    ) {
        ordersCollection.document(orderId)
            .get()
            .addOnSuccessListener { document ->
                val order = document.toObject(Order::class.java)
                Log.d(TAG, "✅ Orden obtenida: $orderId")
                onSuccess(order)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al obtener orden", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    fun getUserOrders(
        userId: String,
        onSuccess: (List<Order>) -> Unit,
        onError: (String) -> Unit
    ) {
        ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val orders = documents.mapNotNull {
                    try {
                        it.toObject(Order::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapeando orden", e)
                        null
                    }
                }
                Log.d(TAG, "✅ Órdenes cargadas: ${orders.size}")
                onSuccess(orders)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al cargar órdenes", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    fun updateOrderStatus(
        orderId: String,
        status: com.techhome.models.OrderStatus,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        ordersCollection.document(orderId)
            .update(
                "status", status,
                "updatedAt", System.currentTimeMillis()
            )
            .addOnSuccessListener {
                Log.d(TAG, "✅ Estado de orden actualizado: $orderId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al actualizar estado", e)
                onError(e.message ?: "Error desconocido")
            }
    }
}