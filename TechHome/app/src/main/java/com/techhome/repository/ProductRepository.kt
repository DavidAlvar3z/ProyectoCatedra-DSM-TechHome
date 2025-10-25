package com.techhome.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.techhome.models.ProductLocal
import com.techhome.network.BestBuyApiService
import com.techhome.network.BestBuyResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class ProductRepository {

    private val db = FirebaseFirestore.getInstance()
    private val apiService = BestBuyApiService.create()
    private val productsCollection = db.collection("products")

    companion object {
        private const val TAG = "ProductRepository"
    }

    // ✅ MÉTODO PARA SEARCHACTIVITY
    fun getAllProducts(
        onSuccess: (List<ProductLocal>) -> Unit,
        onError: (String) -> Unit
    ) {
        productsCollection
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val products = documents.toObjects(ProductLocal::class.java)
                Log.d(TAG, "Total de productos obtenidos: ${products.size}")
                onSuccess(products)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener todos los productos", e)
                onError("Error: ${e.message}")
            }
    }

    // Sincronizar productos con PAGINACIÓN
    fun syncProductsFromBestBuy(
        categoryId: String,
        categoryName: String,
        page: Int = 1,
        pageSize: Int = 20,
        onSuccess: (products: List<ProductLocal>, hasMore: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d(TAG, "Sincronizando página $page de categoría: $categoryId")

        val url = BestBuyApiService.buildCategoryUrl(categoryId, page, pageSize)

        apiService.getProductsByCategory(url)
            .enqueue(object : Callback<BestBuyResponse> {
                override fun onResponse(
                    call: Call<BestBuyResponse>,
                    response: Response<BestBuyResponse>
                ) {
                    if (response.isSuccessful) {
                        val bestBuyResponse = response.body()
                        val products = bestBuyResponse?.products ?: emptyList()
                        val total = bestBuyResponse?.total ?: 0
                        val to = bestBuyResponse?.to ?: 0

                        Log.d(TAG, "Productos obtenidos: ${products.size}, Total: $total, Hasta: $to")

                        val hasMore = to < total

                        saveProductsToFirestore(
                            products,
                            categoryId,
                            categoryName,
                            onSuccess = { localProducts ->
                                onSuccess(localProducts, hasMore)
                            },
                            onError = onError
                        )
                    } else {
                        onError("Error al obtener productos: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<BestBuyResponse>, t: Throwable) {
                    Log.e(TAG, "Error de red", t)
                    onError("Error de conexión: ${t.message}")
                }
            })
    }

    private fun saveProductsToFirestore(
        bestBuyProducts: List<com.techhome.network.Product>,
        categoryId: String,
        categoryName: String,
        onSuccess: (List<ProductLocal>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (bestBuyProducts.isEmpty()) {
            onSuccess(emptyList())
            return
        }

        val batch = db.batch()
        val localProducts = mutableListOf<ProductLocal>()

        bestBuyProducts.forEach { bbProduct ->
            val docRef = productsCollection.document(bbProduct.sku)

            val productLocal = ProductLocal(
                id = bbProduct.sku,
                sku = bbProduct.sku,
                name = bbProduct.name,
                description = generateDescription(bbProduct.name),
                salePrice = bbProduct.salePrice,
                regularPrice = bbProduct.regularPrice,
                image = bbProduct.image,
                url = bbProduct.url,
                categoryId = categoryId,
                categoryName = categoryName,
                stock = Random.nextInt(5, 51),
                lowStockThreshold = 5,
                isAvailable = true,
                lastSyncedFromBestBuy = System.currentTimeMillis(),
                rating = Random.nextDouble(3.5, 5.0),
                reviewCount = Random.nextInt(10, 501),
                brand = extractBrand(bbProduct.name),
                model = extractModel(bbProduct.name)
            )

            batch.set(docRef, productLocal)
            localProducts.add(productLocal)
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d(TAG, "Productos guardados exitosamente: ${localProducts.size}")
                onSuccess(localProducts)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al guardar productos", e)
                onError("Error al guardar: ${e.message}")
            }
    }

    // Obtener productos por categoría desde Firestore
    fun getProductsByCategory(
        categoryId: String,
        onSuccess: (List<ProductLocal>) -> Unit,
        onError: (String) -> Unit
    ) {
        productsCollection
            .whereEqualTo("categoryId", categoryId)
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val products = documents.toObjects(ProductLocal::class.java)
                Log.d(TAG, "Productos obtenidos de Firestore: ${products.size}")
                onSuccess(products)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener productos", e)
                onError("Error: ${e.message}")
            }
    }

    // Verificar si existen productos en una categoría
    fun hasProductsInCategory(
        categoryId: String,
        onResult: (Boolean) -> Unit
    ) {
        productsCollection
            .whereEqualTo("categoryId", categoryId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                onResult(!documents.isEmpty)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    // Obtener un producto específico
    fun getProductBySku(
        sku: String,
        onSuccess: (ProductLocal?) -> Unit,
        onError: (String) -> Unit
    ) {
        productsCollection
            .document(sku)
            .get()
            .addOnSuccessListener { document ->
                val product = document.toObject(ProductLocal::class.java)
                onSuccess(product)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener producto", e)
                onError("Error: ${e.message}")
            }
    }

    // Reducir stock (para simular compra)
    fun decreaseStock(
        sku: String,
        quantity: Int,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val docRef = productsCollection.document(sku)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentStock = snapshot.getLong("stock")?.toInt() ?: 0

            if (currentStock < quantity) {
                throw Exception("Stock insuficiente. Disponible: $currentStock")
            }

            val newStock = currentStock - quantity
            transaction.update(docRef, "stock", newStock)
            transaction.update(docRef, "isAvailable", newStock > 0)

            newStock
        }
            .addOnSuccessListener { newStock ->
                Log.d(TAG, "Stock reducido. Nuevo stock: $newStock")
                onSuccess(newStock)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al reducir stock", e)
                onError(e.message ?: "Error desconocido")
            }
    }

    private fun generateDescription(name: String): String {
        return "Descubre el $name, un producto de alta calidad con las últimas características tecnológicas. " +
                "Perfecto para tu hogar u oficina. Garantía incluida."
    }

    private fun extractBrand(name: String): String {
        val brands = listOf("Apple", "Samsung", "Sony", "LG", "HP", "Dell", "Lenovo",
            "Asus", "Bose", "JBL", "Canon", "Nikon", "Microsoft", "Google", "Xiaomi")
        return brands.firstOrNull { name.contains(it, ignoreCase = true) } ?: "Generic"
    }

    private fun extractModel(name: String): String {
        val modelRegex = Regex("[A-Z0-9]{2,}[-]?[A-Z0-9]+")
        return modelRegex.find(name)?.value ?: "Standard"
    }
}