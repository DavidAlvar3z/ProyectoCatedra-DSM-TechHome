package com.techhome.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.techhome.R
import com.techhome.adapters.CartAdapter
import com.techhome.models.CartItem
import com.techhome.repository.CartRepository
import com.google.firebase.auth.FirebaseAuth

class CartActivity : AppCompatActivity() {

    private lateinit var rvCartItems: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvShipping: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: MaterialButton
    private lateinit var layoutEmptyCart: LinearLayout
    private lateinit var cvSummary: CardView

    private lateinit var cartAdapter: CartAdapter
    private val cartRepository = CartRepository()
    private val shippingCost = 5.0

    companion object {
        private const val TAG = "CartActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        Log.d(TAG, "🎬 onCreate() - Iniciando CartActivity")

        // Configurar toolbar
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            Log.d(TAG, "⬅️ Toolbar back presionado")
            finish()
        }

        initViews()
        setupRecyclerView()
        setupListeners()
        loadCartItems()
    }

    private fun initViews() {
        Log.d(TAG, "🔧 Inicializando vistas...")

        rvCartItems = findViewById(R.id.rvCartItems)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvShipping = findViewById(R.id.tvShipping)
        tvTotal = findViewById(R.id.tvTotal)
        btnCheckout = findViewById(R.id.btnCheckout)
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart)
        cvSummary = findViewById(R.id.cvSummary)

        Log.d(TAG, "✅ Vistas básicas inicializadas")

        // ✅ BUSCAR EL BOTÓN DE EXPLORAR PRODUCTOS
        try {
            val btnExplore = findViewById<MaterialButton>(R.id.btnExploreProducts)
            if (btnExplore != null) {
                Log.d(TAG, "✅ btnExploreProducts encontrado!")

                // Asignar listener INMEDIATAMENTE
                btnExplore.setOnClickListener {
                    Log.d(TAG, "🔘 BOTÓN EXPLORAR PRODUCTOS PRESIONADO!")
                    Toast.makeText(this, "Volviendo al inicio...", Toast.LENGTH_SHORT).show()
                    finish()
                }

                Log.d(TAG, "✅ Listener asignado a btnExploreProducts")
            } else {
                Log.e(TAG, "❌ btnExploreProducts es NULL!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al buscar btnExploreProducts: ${e.message}", e)
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "🔧 Configurando RecyclerView...")

        rvCartItems.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(
            mutableListOf(),
            onQuantityChange = { item, newQuantity ->
                Log.d(TAG, "📊 Cantidad cambiada: ${item.productName} → $newQuantity")
                cartRepository.updateQuantity(
                    getUserId(), item.productSku, newQuantity,
                    onSuccess = {
                        Log.d(TAG, "✅ Cantidad actualizada")
                        loadCartItems()
                    },
                    onError = {
                        Log.e(TAG, "❌ Error actualizando cantidad: $it")
                        showError(it)
                    }
                )
            },
            onRemove = { item ->
                Log.d(TAG, "🗑️ Eliminando: ${item.productName}")
                cartRepository.removeFromCart(
                    getUserId(), item.productSku,
                    onSuccess = {
                        Log.d(TAG, "✅ Producto eliminado")
                        loadCartItems()
                    },
                    onError = {
                        Log.e(TAG, "❌ Error eliminando: $it")
                        showError(it)
                    }
                )
            }
        )
        rvCartItems.adapter = cartAdapter

        Log.d(TAG, "✅ RecyclerView configurado")
    }

    private fun setupListeners() {
        Log.d(TAG, "🔧 Configurando listeners...")

        // ✅ Botón finalizar compra - ABRIR CHECKOUT
        btnCheckout.setOnClickListener {
            Log.d(TAG, "🛒 Botón checkout presionado - Abriendo CheckoutActivity")

            // Verificar que hay productos en el carrito
            if (cartAdapter.itemCount > 0) {
                try {
                    val intent = Intent(this, CheckoutActivity::class.java)
                    startActivity(intent)
                    Log.d(TAG, "✅ CheckoutActivity iniciada correctamente")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al abrir CheckoutActivity: ${e.message}", e)
                    Toast.makeText(
                        this,
                        "Error al abrir checkout. Verifica que CheckoutActivity esté en el Manifest",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Log.w(TAG, "⚠️ Carrito vacío, no se puede proceder al checkout")
                Toast.makeText(this, "Tu carrito está vacío", Toast.LENGTH_SHORT).show()
            }
        }

        Log.d(TAG, "✅ Listeners configurados")
    }

    private fun getUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "demo_user"
    }

    private fun loadCartItems() {
        Log.d(TAG, "🔥 Cargando items del carrito...")

        cartRepository.getCartItems(
            getUserId(),
            onSuccess = { items ->
                Log.d(TAG, "✅ Items cargados: ${items.size}")

                if (items.isEmpty()) {
                    showEmptyCart()
                } else {
                    showCartWithItems(items)
                }
                updateSummary(items)
            },
            onError = {
                Log.e(TAG, "❌ Error cargando items: $it")
                showError(it)
            }
        )
    }

    private fun showEmptyCart() {
        Log.d(TAG, "🔭 Mostrando carrito vacío")

        layoutEmptyCart.visibility = View.VISIBLE
        rvCartItems.visibility = View.GONE
        cvSummary.visibility = View.GONE
        btnCheckout.visibility = View.GONE

        // Verificar que el botón sea visible
        val btnExplore = findViewById<MaterialButton>(R.id.btnExploreProducts)
        Log.d(TAG, "🔍 btnExploreProducts visibility: ${btnExplore?.visibility}")
    }

    private fun showCartWithItems(items: List<CartItem>) {
        Log.d(TAG, "📦 Mostrando carrito con ${items.size} items")

        layoutEmptyCart.visibility = View.GONE
        rvCartItems.visibility = View.VISIBLE
        cvSummary.visibility = View.VISIBLE
        btnCheckout.visibility = View.VISIBLE
        cartAdapter.updateItems(items)
    }

    private fun updateSummary(items: List<CartItem>) {
        val subtotal = cartRepository.calculateCartTotal(items)
        val shipping = if (items.isEmpty()) 0.0 else shippingCost
        val total = subtotal + shipping

        tvSubtotal.text = "$${"%.2f".format(subtotal)}"
        tvShipping.text = "$${"%.2f".format(shipping)}"
        tvTotal.text = "$${"%.2f".format(total)}"

        Log.d(TAG, "💰 Resumen actualizado: Subtotal=$subtotal, Total=$total")
    }

    private fun showError(message: String) {
        Log.e(TAG, "❌ Error: $message")
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "🔄 onResume() - Recargando carrito")
        loadCartItems()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "⏸️ onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "💀 onDestroy()")
    }
}