package com.techhome.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.techhome.R
import com.techhome.adapters.FavoriteAdapter
import com.techhome.repository.FavoriteRepository
import com.techhome.repository.ProductRepository

class FavoritesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var productRepository: ProductRepository

    private lateinit var rvFavorites: RecyclerView
    private lateinit var tvFavoritesCount: TextView
    private lateinit var layoutEmptyFavorites: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageButton
    private lateinit var btnClearAll: ImageButton
    private lateinit var btnStartShopping: MaterialButton

    private lateinit var favoriteAdapter: FavoriteAdapter
    private var isLoading = false // ✅ Bandera para evitar cargas múltiples

    companion object {
        private const val TAG = "FavoritesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        Log.d(TAG, "🎬 onCreate() - Iniciando FavoritesActivity")

        auth = FirebaseAuth.getInstance()
        favoriteRepository = FavoriteRepository()
        productRepository = ProductRepository()

        initViews()
        setupRecyclerView()
        setupListeners()

        // ✅ SOLO cargar una vez en onCreate
        loadFavorites()
    }

    private fun initViews() {
        rvFavorites = findViewById(R.id.rvFavorites)
        tvFavoritesCount = findViewById(R.id.tvFavoritesCount)
        layoutEmptyFavorites = findViewById(R.id.layoutEmptyFavorites)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)
        btnClearAll = findViewById(R.id.btnClearAll)
        btnStartShopping = findViewById(R.id.btnStartShopping)
    }

    private fun setupRecyclerView() {
        rvFavorites.layoutManager = GridLayoutManager(this, 2)
        favoriteAdapter = FavoriteAdapter(
            favorites = emptyList(),
            onItemClick = { favorite ->
                openProductDetail(favorite.productSku)
            },
            onRemoveClick = { favorite ->
                showRemoveDialog(favorite.productSku, favorite.productName)
            }
        )
        rvFavorites.adapter = favoriteAdapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            Log.d(TAG, "⬅️ Botón atrás presionado")
            finish()
        }

        btnClearAll.setOnClickListener {
            Log.d(TAG, "🗑️ Limpiar todos presionado")
            showClearAllDialog()
        }

        btnStartShopping.setOnClickListener {
            Log.d(TAG, "🛍️ Explorar productos presionado")
            finish()
        }
    }

    private fun loadFavorites() {
        // ✅ Verificar que no estemos ya cargando
        if (isLoading) {
            Log.w(TAG, "⚠️ Ya se está cargando favoritos, ignorando llamada duplicada")
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "❌ Usuario no autenticado")
            showEmptyState()
            return
        }

        Log.d(TAG, "📥 Iniciando carga de favoritos para userId: $userId")
        isLoading = true
        showLoading()

        favoriteRepository.getFavorites(
            userId = userId,
            onSuccess = { favorites ->
                isLoading = false
                Log.d(TAG, "✅ Favoritos cargados exitosamente: ${favorites.size}")

                if (favorites.isEmpty()) {
                    showEmptyState()
                } else {
                    showFavorites(favorites.size)
                    favoriteAdapter.updateFavorites(favorites)
                }
            },
            onError = { error ->
                isLoading = false
                Log.e(TAG, "❌ Error al cargar favoritos: $error")
                showEmptyState()
            }
        )
    }

    private fun showLoading() {
        Log.d(TAG, "⏳ Mostrando loading")
        progressBar.visibility = View.VISIBLE
        rvFavorites.visibility = View.GONE
        layoutEmptyFavorites.visibility = View.GONE
        btnClearAll.visibility = View.GONE
    }

    private fun showFavorites(count: Int) {
        Log.d(TAG, "📦 Mostrando $count favoritos")
        progressBar.visibility = View.GONE
        rvFavorites.visibility = View.VISIBLE
        layoutEmptyFavorites.visibility = View.GONE
        btnClearAll.visibility = View.VISIBLE

        tvFavoritesCount.text = if (count == 1) {
            "1 producto favorito"
        } else {
            "$count productos favoritos"
        }
    }

    private fun showEmptyState() {
        Log.d(TAG, "📭 Mostrando estado vacío")
        progressBar.visibility = View.GONE
        rvFavorites.visibility = View.GONE
        layoutEmptyFavorites.visibility = View.VISIBLE
        btnClearAll.visibility = View.GONE
        tvFavoritesCount.text = "0 productos favoritos"
    }

    private fun removeFromFavorites(productSku: String, productName: String) {
        val userId = auth.currentUser?.uid ?: return

        Log.d(TAG, "🗑️ Eliminando de favoritos: $productName")

        favoriteRepository.removeFromFavorites(
            userId = userId,
            productSku = productSku,
            onSuccess = {
                Log.d(TAG, "✅ Producto eliminado exitosamente")
                // ✅ Recargar lista después de eliminar
                loadFavorites()
            },
            onError = { error ->
                Log.e(TAG, "❌ Error al eliminar: $error")
            }
        )
    }

    private fun showRemoveDialog(productSku: String, productName: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar favorito")
            .setMessage("¿Deseas eliminar \"$productName\" de tus favoritos?")
            .setPositiveButton("Eliminar") { _, _ ->
                removeFromFavorites(productSku, productName)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showClearAllDialog() {
        val currentCount = favoriteAdapter.itemCount
        if (currentCount == 0) return

        AlertDialog.Builder(this)
            .setTitle("Limpiar favoritos")
            .setMessage("¿Deseas eliminar todos los $currentCount productos de tus favoritos?")
            .setPositiveButton("Eliminar todos") { _, _ ->
                clearAllFavorites()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun clearAllFavorites() {
        val userId = auth.currentUser?.uid ?: return

        Log.d(TAG, "🗑️ Eliminando todos los favoritos")
        showLoading()

        // Obtener lista actual y eliminar uno por uno
        favoriteRepository.getFavorites(
            userId = userId,
            onSuccess = { favorites ->
                if (favorites.isEmpty()) {
                    showEmptyState()
                    return@getFavorites
                }

                var deletedCount = 0
                val totalToDelete = favorites.size

                favorites.forEach { favorite ->
                    favoriteRepository.removeFromFavorites(
                        userId = userId,
                        productSku = favorite.productSku,
                        onSuccess = {
                            deletedCount++
                            if (deletedCount == totalToDelete) {
                                Log.d(TAG, "✅ Todos los favoritos eliminados")
                                showEmptyState()
                            }
                        },
                        onError = { error ->
                            Log.e(TAG, "❌ Error al eliminar: $error")
                        }
                    )
                }
            },
            onError = { error ->
                Log.e(TAG, "❌ Error al obtener favoritos: $error")
                showEmptyState()
            }
        )
    }

    private fun openProductDetail(sku: String) {
        Log.d(TAG, "🔍 Abriendo detalle del producto: $sku")
        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_SKU, sku)
        }
        startActivity(intent)
    }

    // ✅ NO recargar en onResume - ya se carga en onCreate
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "🔄 onResume() - NO recargando favoritos")
        // NO llamar loadFavorites() aquí
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "💀 onDestroy()")
    }
} 