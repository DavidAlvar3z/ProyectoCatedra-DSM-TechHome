package com.techhome.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.techhome.R
import com.techhome.adapters.ProductLocalAdapter
import com.techhome.models.ProductLocal
import com.techhome.repository.ProductRepository

class ProductsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnSync: MaterialButton
    private lateinit var productAdapter: ProductLocalAdapter
    private val repository = ProductRepository()

    private var categoryId: String = ""
    private var categoryName: String = ""

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

        setupRecyclerView()
        setupSyncButton()

        loadProductsFromFirestore()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rvProducts)
        progressBar = findViewById(R.id.progressBar)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        productAdapter = ProductLocalAdapter(emptyList()) { product ->
            openProductDetail(product)
        }
        recyclerView.adapter = productAdapter
    }

    private fun setupSyncButton() {
        btnSync = findViewById(R.id.btnSync)
        btnSync.setOnClickListener {
            syncProductsFromBestBuy()
        }
    }

    private fun loadProductsFromFirestore() {
        showLoading(true)

        repository.getProductsByCategory(
            categoryId = categoryId,
            onSuccess = { products ->
                showLoading(false)

                if (products.isEmpty()) {
                    Toast.makeText(
                        this,
                        "No hay productos. Presiona 'Sincronizar' para cargar desde Best Buy",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    productAdapter.updateProducts(products)
                    Toast.makeText(
                        this,
                        "${products.size} productos cargados",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun syncProductsFromBestBuy() {
        showLoading(true)
        btnSync.isEnabled = false

        Toast.makeText(
            this,
            "Sincronizando productos desde Best Buy...",
            Toast.LENGTH_SHORT
        ).show()

        repository.syncProductsFromBestBuy(
            categoryId = categoryId,
            categoryName = categoryName,
            onSuccess = { products ->
                showLoading(false)
                btnSync.isEnabled = true

                productAdapter.updateProducts(products)
                Toast.makeText(
                    this,
                    "✅ ${products.size} productos sincronizados exitosamente",
                    Toast.LENGTH_LONG
                ).show()
            },
            onError = { error ->
                showLoading(false)
                btnSync.isEnabled = true
                Toast.makeText(this, "❌ Error: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun openProductDetail(product: ProductLocal) {
        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_SKU, product.sku)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadProductsFromFirestore()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}