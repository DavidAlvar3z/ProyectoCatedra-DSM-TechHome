package com.techhome.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.techhome.R
import com.techhome.models.ProductLocal
import com.techhome.models.StockStatus
import com.techhome.repository.ProductRepository
import com.techhome.repository.CartRepository
import com.techhome.repository.FavoriteRepository
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var repository: ProductRepository
    private lateinit var cartRepository: CartRepository
    private lateinit var favoriteRepository: FavoriteRepository

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
    private lateinit var btnFavorite: ImageButton

    private var currentProduct: ProductLocal? = null
    private var isFavorite = false

    // Handler para cambiar ofertas cada 10 segundos
    private val handler = Handler(Looper.getMainLooper())
    private val discountRunnable = object : Runnable {
        override fun run() {
            updateDiscountBadge()
            handler.postDelayed(this, 10000) // Cada 10 segundos
        }
    }

    companion object {
        const val EXTRA_PRODUCT_SKU = "product_sku"
        private const val TAG = "ProductDetailActivity"
        private const val CHANNEL_ID = "purchase_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        repository = ProductRepository()
        cartRepository = CartRepository()
        favoriteRepository = FavoriteRepository()

        createNotificationChannel()
        initViews()
        setupListeners()

        val sku = intent.getStringExtra(EXTRA_PRODUCT_SKU)
        if (sku != null) {
            loadProductDetails(sku)
        } else {
            Toast.makeText(this, "Error: Producto no encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Iniciar cambio de ofertas
        handler.post(discountRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(discountRunnable)
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
        btnFavorite = findViewById(R.id.btnFavorite)
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

        btnFavorite.setOnClickListener {
            toggleFavorite()
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
                    checkIfFavorite(sku)
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

    private fun checkIfFavorite(sku: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            favoriteRepository.isFavorite(user.uid, sku) { favorite ->
                isFavorite = favorite
                updateFavoriteButton()
            }
        }
    }

    private fun updateFavoriteButton() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_filled)
            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.error))
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_outline)
            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    private fun toggleFavorite() {
        val user = FirebaseAuth.getInstance().currentUser
        val product = currentProduct

        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión para usar favoritos", Toast.LENGTH_SHORT).show()
            return
        }

        if (product == null) return

        favoriteRepository.toggleFavorite(
            userId = user.uid,
            product = product,
            onSuccess = { added ->
                isFavorite = added
                updateFavoriteButton()
                val message = if (added) "❤️ Agregado a favoritos" else "💔 Eliminado de favoritos"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
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
            updateDiscountBadge()
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

    private fun updateDiscountBadge() {
        val product = currentProduct ?: return
        val discount = product.getDiscountPercentage()
        if (discount > 0) {
            val messages = listOf(
                "-$discount% OFF",
                "¡OFERTA! -$discount%",
                "🔥 AHORRA $discount%",
                "⚡ -$discount% HOY",
                "💥 DESCUENTO $discount%"
            )
            chipDiscount.text = messages.random()
        }
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
            showLoading(true)

            repository.decreaseStock(
                sku = product.sku,
                quantity = 1,
                onSuccess = { newStock ->
                    showLoading(false)

                    // Enviar notificación de compra
                    sendPurchaseNotification(product)

                    Toast.makeText(
                        this,
                        "🎉 ¡Compra realizada con éxito!",
                        Toast.LENGTH_LONG
                    ).show()

                    val updatedProduct = product.copy(stock = newStock)
                    currentProduct = updatedProduct
                    updateStockUI(updatedProduct)
                },
                onError = { error ->
                    showLoading(false)
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Compras"
            val descriptionText = "Notificaciones de compras realizadas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendPurchaseNotification(product: ProductLocal) {
        val intent = Intent(this, CartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val formatter = NumberFormat.getCurrencyInstance(Locale.US)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shopping_bag)
            .setContentTitle("🎉 ¡Compra Exitosa!")
            .setContentText("${product.name} - ${formatter.format(product.salePrice)}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Has comprado: ${product.name}\nPrecio: ${formatter.format(product.salePrice)}\n\n¡Gracias por tu compra!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
            }
        } else {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}