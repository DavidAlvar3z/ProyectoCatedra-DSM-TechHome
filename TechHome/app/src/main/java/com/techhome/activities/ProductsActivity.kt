package com.techhome.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.techhome.R
import com.techhome.adapters.ProductLocalAdapter
import com.techhome.models.ProductLocal
import com.techhome.repository.ProductRepository

class ProductsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBarBottom: ProgressBar
    private lateinit var tvNoProducts: View
    private lateinit var tvAllProductsLoaded: TextView
    private lateinit var productAdapter: ProductLocalAdapter
    private val repository = ProductRepository()

    private var categoryId: String = ""
    private var categoryName: String = ""

    private val allProducts = mutableListOf<ProductLocal>()
    private var isLoading = false
    private var isLastPage = false
    private var currentPage = 1

    companion object {
        const val EXTRA_CATEGORY_ID = "category_id"
        const val EXTRA_CATEGORY_NAME = "category_name"
        private const val TAG = "ProductsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID) ?: ""
        categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME) ?: "Productos"

        supportActionBar?.title = categoryName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        setupRecyclerView()
        checkAndLoadProducts()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvProducts)
        progressBar = findViewById(R.id.progressBar)
        progressBarBottom = findViewById(R.id.progressBarBottom)
        tvNoProducts = findViewById(R.id.tvNoProducts)
        tvAllProductsLoaded = findViewById(R.id.tvAllProductsLoaded)
    }

    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = gridLayoutManager

        productAdapter = ProductLocalAdapter(emptyList()) { product ->
            openProductDetail(product)
        }
        recyclerView.adapter = productAdapter

        // ✅ SCROLL LISTENER PARA PAGINACIÓN INFINITA
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = gridLayoutManager.childCount
                val totalItemCount = gridLayoutManager.itemCount
                val firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition()

                // ✅ Detectar cuando llegamos al final
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= 2) {  // Al menos 2 items para activar scroll

                        Log.d(TAG, "📜 Llegamos al final, cargando más productos...")
                        loadMoreProducts()
                    }
                }
            }
        })
    }

    private fun checkAndLoadProducts() {
        showMainLoading(true)

        // Verificar si hay productos en Firestore
        repository.hasProductsInCategory(categoryId) { hasProducts ->
            if (hasProducts) {
                // Ya hay productos, cargarlos
                loadProductsFromFirestore()
            } else {
                // No hay productos, sincronizar desde Best Buy
                syncFromBestBuy()
            }
        }
    }

    private fun loadProductsFromFirestore() {
        repository.getProductsByCategory(
            categoryId = categoryId,
            onSuccess = { products ->
                showMainLoading(false)

                if (products.isEmpty()) {
                    // No hay productos, sincronizar
                    syncFromBestBuy()
                } else {
                    allProducts.clear()
                    allProducts.addAll(products)
                    productAdapter.updateProducts(allProducts)
                    updateUI()
                }
            },
            onError = { error ->
                showMainLoading(false)
                Log.e(TAG, "Error al cargar desde Firestore: $error")
                // Si falla, intentar sincronizar
                syncFromBestBuy()
            }
        )
    }

    private fun syncFromBestBuy() {
        if (isLoading) return

        isLoading = true

        if (currentPage == 1) {
            showMainLoading(true)
        } else {
            showBottomLoading(true)
        }

        Log.d(TAG, "🔄 Sincronizando página $currentPage...")

        repository.syncProductsFromBestBuy(
            categoryId = categoryId,
            categoryName = categoryName,
            page = currentPage,
            pageSize = 20,
            onSuccess = { products, hasMore ->
                isLoading = false
                showMainLoading(false)
                showBottomLoading(false)

                if (products.isEmpty() && allProducts.isEmpty()) {
                    // No hay productos en absoluto
                    tvNoProducts.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    Toast.makeText(this, "No hay productos disponibles en esta categoría", Toast.LENGTH_LONG).show()
                } else {
                    allProducts.addAll(products)
                    productAdapter.updateProducts(allProducts)

                    isLastPage = !hasMore

                    if (isLastPage) {
                        showAllProductsLoadedMessage()
                        Log.d(TAG, "✅ Todos los productos cargados. Total: ${allProducts.size}")
                    } else {
                        Log.d(TAG, "📦 Cargados ${products.size} productos. Total acumulado: ${allProducts.size}")
                    }

                    updateUI()

                    if (currentPage == 1) {
                        Toast.makeText(
                            this,
                            "✅ ${allProducts.size} productos cargados",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            onError = { error ->
                isLoading = false
                showMainLoading(false)
                showBottomLoading(false)

                Log.e(TAG, "❌ Error al sincronizar: $error")
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()

                // Si es la primera página y falla, mostrar mensaje
                if (currentPage == 1 && allProducts.isEmpty()) {
                    tvNoProducts.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            }
        )
    }

    private fun loadMoreProducts() {
        if (isLoading || isLastPage) return

        currentPage++
        syncFromBestBuy()
    }

    private fun updateUI() {
        if (allProducts.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvNoProducts.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvNoProducts.visibility = View.GONE
        }
    }

    private fun showAllProductsLoadedMessage() {
        tvAllProductsLoaded.visibility = View.VISIBLE
        tvAllProductsLoaded.postDelayed({
            tvAllProductsLoaded.visibility = View.GONE
        }, 3000)
    }

    private fun openProductDetail(product: ProductLocal) {
        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_SKU, product.sku)
        }
        startActivity(intent)
    }

    private fun showMainLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showBottomLoading(show: Boolean) {
        progressBarBottom.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}