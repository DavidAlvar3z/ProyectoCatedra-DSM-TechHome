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
        val userCartRef = cartCollection.document(userId).collection("items").document(product.id)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userCartRef)
            val currentQuantity = snapshot.getLong("quantity")?.toInt() ?: 0
            val newQuantity = currentQuantity + quantity

            val cartItem = mapOf(
                "productId" to product.id,
                "name" to product.name,
                "image" to product.image,
                "salePrice" to product.salePrice,
                "regularPrice" to product.regularPrice,
                "quantity" to newQuantity,
                "categoryId" to product.categoryId,
                "categoryName" to product.categoryName,
                "timestamp" to System.currentTimeMillis()
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
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                // Convertir cada documento en CartItem
                val items = documents.mapNotNull { doc ->
                    try {
                        CartItem(
                            productId = doc.getString("productId") ?: "",
                            name = doc.getString("name") ?: "Producto",
                            image = doc.getString("image") ?: "",
                            salePrice = doc.getDouble("salePrice") ?: 0.0,
                            regularPrice = doc.getDouble("regularPrice") ?: 0.0,
                            quantity = (doc.getLong("quantity") ?: 1).toInt(),
                            categoryId = doc.getString("categoryId") ?: "",
                            categoryName = doc.getString("categoryName") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al mapear CartItem", e)
                        null
                    }
                }
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
        productId: String,
        newQuantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val itemRef = cartCollection.document(userId).collection("items").document(productId)
        val safeQuantity = max(newQuantity, 1)

        itemRef.update("quantity", safeQuantity)
            .addOnSuccessListener {
                Log.d(TAG, "Cantidad actualizada: $safeQuantity")
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al actualizar cantidad")
            }
    }

    /**
     * 🔹 Eliminar producto del carrito
     */
    fun removeFromCart(
        userId: String,
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val itemRef = cartCollection.document(userId).collection("items").document(productId)

        itemRef.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Producto eliminado del carrito: $productId")
                onSuccess()
            }
            .addOnFailureListener { e ->
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
                        onError(e.message ?: "Error al vaciar carrito")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al acceder al carrito")
            }
    }

    /**
     * 🔹 Calcular total del carrito
     */
    fun calculateCartTotal(items: List<CartItem>): Double {
        return items.sumOf { it.salePrice * it.quantity }
    }
}
