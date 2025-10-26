package com.techhome.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.techhome.models.ProductLocal
import com.techhome.models.CartItem
import kotlin.math.max

class CartRepository {

    private val db = FirebaseFirestore.getInstance()
    private val cartCollection = db.collection("carts")

    companion object {
        private const val TAG = "CartRepository"
    }

    /**
     * 🔹 Agregar producto al carrito del usuario
     */
    fun addToCart(
        userId: String,
        product: ProductLocal,
        quantity: Int = 1,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cartItemId = "${userId}_${product.sku}"
        val userCartRef = cartCollection.document(userId).collection("items").document(product.sku)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userCartRef)
            val currentQuantity = snapshot.getLong("quantity")?.toInt() ?: 0
            val newQuantity = currentQuantity + quantity

            val cartItem = hashMapOf(
                "cartItemId" to cartItemId,
                "productSku" to product.sku,
                "productName" to product.name,
                "productImage" to product.image,
                "price" to product.salePrice,
                "regularPrice" to product.regularPrice,
                "quantity" to newQuantity,
                "addedAt" to System.currentTimeMillis()
            )

            transaction.set(userCartRef, cartItem)
        }.addOnSuccessListener {
            Log.d(TAG, "Producto agregado al carrito: ${product.name}")
            onSuccess()
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error al agregar producto al carrito", e)
            onError(e.message ?: "Error desconocido")
        }
    }

    /**
     * 🔹 Obtener todos los productos del carrito de un usuario
     */
    fun getCartItems(
        userId: String,
        onSuccess: (List<CartItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        cartCollection.document(userId).collection("items")
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val items = documents.mapNotNull { doc ->
                    try {
                        CartItem(
                            cartItemId = doc.getString("cartItemId") ?: "",
                            productSku = doc.getString("productSku") ?: "",
                            productName = doc.getString("productName") ?: "Producto",
                            productImage = doc.getString("productImage") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            regularPrice = doc.getDouble("regularPrice") ?: 0.0,
                            quantity = (doc.getLong("quantity") ?: 1).toInt(),
                            addedAt = doc.getLong("addedAt") ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al mapear CartItem", e)
                        null
                    }
                }
                Log.d(TAG, "Carrito cargado: ${items.size} items")
                onSuccess(items)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener carrito", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    /**
     * 🔹 Actualizar cantidad de un producto en el carrito
     */
    fun updateQuantity(
        userId: String,
        productSku: String,
        newQuantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val itemRef = cartCollection.document(userId).collection("items").document(productSku)
        val safeQuantity = max(newQuantity, 1)

        itemRef.update("quantity", safeQuantity)
            .addOnSuccessListener {
                Log.d(TAG, "Cantidad actualizada: $safeQuantity")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al actualizar cantidad", e)
                onError(e.message ?: "Error al actualizar cantidad")
            }
    }

    /**
     * 🔹 Eliminar producto del carrito
     */
    fun removeFromCart(
        userId: String,
        productSku: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val itemRef = cartCollection.document(userId).collection("items").document(productSku)

        itemRef.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Producto eliminado del carrito: $productSku")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al eliminar producto", e)
                onError(e.message ?: "Error al eliminar producto")
            }
    }

    /**
     * 🔹 Vaciar carrito completo del usuario
     */
    fun clearCart(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userCartRef = cartCollection.document(userId).collection("items")
        userCartRef.get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d(TAG, "Carrito vaciado con éxito")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error al vaciar carrito", e)
                        onError(e.message ?: "Error al vaciar carrito")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al acceder al carrito", e)
                onError(e.message ?: "Error al acceder al carrito")
            }
    }

    /**
     * 🔹 Calcular total del carrito
     */
    fun calculateCartTotal(items: List<CartItem>): Double {
        return items.sumOf { it.price * it.quantity }
    }

    /**
     * 🔹 Calcular subtotal (sin descuentos)
     */
    fun calculateSubtotal(items: List<CartItem>): Double {
        return items.sumOf { it.regularPrice * it.quantity }
    }

    /**
     * 🔹 Calcular ahorro total
     */
    fun calculateTotalSavings(items: List<CartItem>): Double {
        return items.sumOf { (it.regularPrice - it.price) * it.quantity }
    }
}