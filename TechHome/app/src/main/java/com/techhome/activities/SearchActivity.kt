package com.techhome.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.techhome.R
import com.techhome.adapters.ProductLocalAdapter
import com.techhome.models.ProductLocal
import com.techhome.repository.ProductRepository

class SearchActivity : AppCompatActivity() {

    private lateinit var etSearch: TextInputEditText
    private lateinit var rvResults: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoResults: View

    private lateinit var productAdapter: ProductLocalAdapter
    private val repository = ProductRepository()

    private var allProducts = listOf<ProductLocal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupSearchBar()
        setupRecyclerView()
        loadAllProducts()
    }

    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        rvResults = findViewById(R.id.rvResults)
        progressBar = findViewById(R.id.progressBar)
        tvNoResults = findViewById(R.id.tvNoResults)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupSearchBar() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                filterProducts(query)
            }
        })
    }

    private fun setupRecyclerView() {
        rvResults.layoutManager = GridLayoutManager(this, 2)
        productAdapter = ProductLocalAdapter(emptyList()) { product ->
            openProductDetail(product)
        }
        rvResults.adapter = productAdapter
    }

    private fun loadAllProducts() {
        showLoading(true)

        repository.getAllProducts(
            onSuccess = { products ->
                showLoading(false)
                allProducts = products
                filterProducts("")
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun filterProducts(query: String) {
        val filtered = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.brand.contains(query, ignoreCase = true) ||
                        it.categoryName.contains(query, ignoreCase = true)
            }
        }

        updateUI(filtered)
    }

    private fun updateUI(products: List<ProductLocal>) {
        if (products.isEmpty()) {
            rvResults.visibility = View.GONE
            tvNoResults.visibility = View.VISIBLE
        } else {
            rvResults.visibility = View.VISIBLE
            tvNoResults.visibility = View.GONE
        }

        productAdapter.updateProducts(products)
    }

    private fun openProductDetail(product: ProductLocal) {
        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_SKU, product.sku)
        }
        startActivity(intent)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}