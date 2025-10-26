package com.techhome.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.techhome.R
import com.techhome.models.ProductLocal
import com.techhome.models.StockStatus
import com.techhome.repository.ProductRepository
import com.techhome.repository.CartRepository // 🛒 Import agregado
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var repository: ProductRepository
    private lateinit var cartRepository: CartRepository // 🛒 Repositorio del carrito

    private lateinit var progressBar: ProgressBar
    private lateinit var ivProductImage: ImageView
    private lateinit var tvProductName: TextView
    private lateinit var tvProductPrice: TextView
    private lateinit var tvRegularPrice: TextView
    private lateinit var chipDiscount: Chip
    private lateinit var tvDescription: TextView
    private lateinit var tvBrand: TextView
    private lateinit var tvModel: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvReviewCount: TextView
    private lateinit var tvStockStatus: TextView
    private lateinit var tvStockCount: TextView
    private lateinit var cvStockInfo: CardView
    private lateinit var btnAddToCart: MaterialButton
    private lateinit var btnBuyNow: MaterialButton

    private var currentProduct: ProductLocal? = null

    companion object {
        const val EXTRA_PRODUCT_SKU = "product_sku"
        private const val TAG = "ProductDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        repository = ProductRepository()
        cartRepository = CartRepository() // 🛒 Inicializamos el carrito

        initViews()
        setupListeners()

        val sku = intent.getStringExtra(EXTRA_PRODUCT_SKU)
        if (sku != null) {
            loadProductDetails(sku)
        } else {
            Toast.makeText(this, "Error: Producto no encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        ivProductImage = findViewById(R.id.ivProductImage)
        tvProductName = findViewById(R.id.tvProductName)
        tvProductPrice = findViewById(R.id.tvProductPrice)
        tvRegularPrice = findViewById(R.id.tvRegularPrice)
        chipDiscount = findViewById(R.id.chipDiscount)
        tvDescription = findViewById(R.id.tvDescription)
        tvBrand = findViewById(R.id.tvBrand)
        tvModel = findViewById(R.id.tvModel)
        tvRating = findViewById(R.id.tvRating)
        tvReviewCount = findViewById(R.id.tvReviewCount)
        tvStockStatus = findViewById(R.id.tvStockStatus)
        tvStockCount = findViewById(R.id.tvStockCount)
        cvStockInfo = findViewById(R.id.cvStockInfo)
        btnAddToCart = findViewById(R.id.btnAddToCart)
        btnBuyNow = findViewById(R.id.btnBuyNow)
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnAddToCart.setOnClickListener {
            addToCart()
        }

        btnBuyNow.setOnClickListener {
            buyNow()
        }
    }

    private fun loadProductDetails(sku: String) {
        showLoading(true)

        repository.getProductBySku(
            sku = sku,
            onSuccess = { product ->
                showLoading(false)
                if (product != null) {
                    currentProduct = product
                    displayProduct(product)
                } else {
                    Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                finish()
            }
        )
    }

    private fun displayProduct(product: ProductLocal) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)

        tvProductName.text = product.name
        tvProductPrice.text = formatter.format(product.salePrice)

        if (product.regularPrice > product.salePrice) {
            tvRegularPrice.visibility = View.VISIBLE
            tvRegularPrice.text = formatter.format(product.regularPrice)
            tvRegularPrice.paintFlags = tvRegularPrice.paintFlags or
                    android.graphics.Paint.STRIKE_THRU_TEXT_FLAG

            chipDiscount.visibility = View.VISIBLE
            chipDiscount.text = "${product.getDiscountPercentage()}% OFF"
        } else {
            tvRegularPrice.visibility = View.GONE
            chipDiscount.visibility = View.GONE
        }

        Glide.with(this)
            .load(product.image)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(ivProductImage)

        tvDescription.text = product.description
        tvBrand.text = "Marca: ${product.brand}"
        tvModel.text = "Modelo: ${product.model}"
        tvRating.text = String.format("%.1f ⭐", product.rating)
        tvReviewCount.text = "(${product.reviewCount} reseñas)"

        updateStockUI(product)
    }

    private fun updateStockUI(product: ProductLocal) {
        when (product.getStockStatus()) {
            StockStatus.IN_STOCK -> {
                tvStockStatus.text = "En Stock"
                tvStockStatus.setTextColor(getColor(R.color.green_600))
                tvStockCount.text = "${product.stock} unidades disponibles"
                cvStockInfo.setCardBackgroundColor(getColor(R.color.green_50))
                btnAddToCart.isEnabled = true
                btnBuyNow.isEnabled = true
            }
            StockStatus.LOW_STOCK -> {
                tvStockStatus.text = "¡Últimas unidades!"
                tvStockStatus.setTextColor(getColor(R.color.orange_600))
                tvStockCount.text = "Solo quedan ${product.stock} unidades"
                cvStockInfo.setCardBackgroundColor(getColor(R.color.orange_50))
                btnAddToCart.isEnabled = true
                btnBuyNow.isEnabled = true
            }
            StockStatus.OUT_OF_STOCK -> {
                tvStockStatus.text = "Agotado"
                tvStockStatus.setTextColor(getColor(R.color.red_600))
                tvStockCount.text = "Producto no disponible"
                cvStockInfo.setCardBackgroundColor(getColor(R.color.red_50))
                btnAddToCart.isEnabled = false
                btnBuyNow.isEnabled = false
            }
        }
    }

    // 🛒 Nueva versión con integración de carrito y actualización de stock
    private fun addToCart() {
        currentProduct?.let { product ->
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(this, "Debes iniciar sesión para agregar al carrito", Toast.LENGTH_SHORT).show()
                return
            }

            showLoading(true)

            cartRepository.addToCart(
                userId = user.uid,
                product = product,
                quantity = 1,
                onSuccess = {
                    repository.decreaseStock(
                        sku = product.sku,
                        quantity = 1,
                        onSuccess = { newStock ->
                            showLoading(false)
                            val updatedProduct = product.copy(stock = newStock)
                            currentProduct = updatedProduct
                            updateStockUI(updatedProduct)
                            Toast.makeText(this, "🛒 Agregado al carrito", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            showLoading(false)
                            Toast.makeText(this, "Error al actualizar stock: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onError = { error ->
                    showLoading(false)
                    Toast.makeText(this, "Error al agregar: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun buyNow() {
        currentProduct?.let { product ->
            repository.decreaseStock(
                sku = product.sku,
                quantity = 1,
                onSuccess = { newStock ->
                    Toast.makeText(
                        this,
                        "🎉 Compra realizada con éxito!",
                        Toast.LENGTH_LONG
                    ).show()

                    val updatedProduct = product.copy(stock = newStock)
                    currentProduct = updatedProduct
                    updateStockUI(updatedProduct)
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun openBestBuyUrl() {
        currentProduct?.let { product ->
            if (product.url.isNotEmpty()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = android.net.Uri.parse(product.url)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al abrir enlace", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}
