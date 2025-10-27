package com.techhome.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.techhome.models.Favorite
import com.techhome.models.ProductLocal

class FavoriteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val favoritesCollection = db.collection("favorites")

    companion object {
        private const val TAG = "FavoriteRepository"
    }

    /**
     * Agregar producto a favoritos (SIN DUPLICADOS)
     */
    fun addToFavorites(
        userId: String,
        product: ProductLocal,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val favoriteRef = favoritesCollection
            .document(userId)
            .collection("items")
            .document(product.sku) // ✅ Usar SKU como ID evita duplicados

        val favorite = hashMapOf(
            "favoriteId" to "${userId}_${product.sku}",
            "userId" to userId,
            "productSku" to product.sku,
            "productName" to product.name,
            "productImage" to product.image,
            "price" to product.salePrice,
            "addedAt" to System.currentTimeMillis()
        )

        // ✅ set() sobrescribe si existe, evitando duplicados
        favoriteRef.set(favorite)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Producto agregado/actualizado en favoritos: ${product.name}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al agregar a favoritos", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    /**
     * Eliminar producto de favoritos
     */
    fun removeFromFavorites(
        userId: String,
        productSku: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        favoritesCollection.document(userId)
            .collection("items")
            .document(productSku)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "🗑️ Producto eliminado de favoritos: $productSku")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al eliminar de favoritos", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    /**
     * Verificar si un producto está en favoritos
     */
    fun isFavorite(
        userId: String,
        productSku: String,
        onResult: (Boolean) -> Unit
    ) {
        favoritesCollection.document(userId)
            .collection("items")
            .document(productSku)
            .get()
            .addOnSuccessListener { document ->
                onResult(document.exists())
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    /**
     * Obtener todos los favoritos (SIN DUPLICADOS)
     */
    fun getFavorites(
        userId: String,
        onSuccess: (List<Favorite>) -> Unit,
        onError: (String) -> Unit
    ) {
        favoritesCollection.document(userId)
            .collection("items")
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val favorites = documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Favorite::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al mapear favorito", e)
                        null
                    }
                }

                // ✅ Eliminar duplicados por SKU (por si acaso)
                val uniqueFavorites = favorites.distinctBy { it.productSku }

                Log.d(TAG, "📦 Favoritos cargados: ${uniqueFavorites.size}")
                onSuccess(uniqueFavorites)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al obtener favoritos", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    /**
     * Toggle favorito (agregar/eliminar)
     */
    fun toggleFavorite(
        userId: String,
        product: ProductLocal,
        onSuccess: (Boolean) -> Unit, // true = agregado, false = eliminado
        onError: (String) -> Unit
    ) {
        isFavorite(userId, product.sku) { isFav ->
            if (isFav) {
                // Ya está en favoritos, eliminar
                removeFromFavorites(userId, product.sku,
                    onSuccess = { onSuccess(false) },
                    onError = onError
                )
            } else {
                // No está en favoritos, agregar
                addToFavorites(userId, product,
                    onSuccess = { onSuccess(true) },
                    onError = onError
                )
            }
        }
    }

    /**
     * 🆕 Limpiar duplicados existentes (función de utilidad)
     */
    fun cleanDuplicates(
        userId: String,
        onSuccess: (Int) -> Unit, // Retorna cuántos duplicados eliminó
        onError: (String) -> Unit
    ) {
        favoritesCollection.document(userId)
            .collection("items")
            .get()
            .addOnSuccessListener { documents ->
                val favorites = documents.mapNotNull { doc ->
                    try {
                        Pair(doc.id, doc.toObject(Favorite::class.java))
                    } catch (e: Exception) {
                        null
                    }
                }

                // Agrupar por SKU
                val grouped = favorites.groupBy { it.second.productSku }
                var deletedCount = 0

                // Para cada SKU que tiene duplicados
                grouped.forEach { (sku, items) ->
                    if (items.size > 1) {
                        // Mantener el más reciente, eliminar los demás
                        val sortedItems = items.sortedByDescending { it.second.addedAt }
                        sortedItems.drop(1).forEach { (docId, _) ->
                            favoritesCollection.document(userId)
                                .collection("items")
                                .document(docId)
                                .delete()
                            deletedCount++
                        }
                    }
                }

                Log.d(TAG, "🧹 Duplicados eliminados: $deletedCount")
                onSuccess(deletedCount)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al limpiar duplicados", e)
                onError(e.message ?: "Error desconocido")
            }
    }
}