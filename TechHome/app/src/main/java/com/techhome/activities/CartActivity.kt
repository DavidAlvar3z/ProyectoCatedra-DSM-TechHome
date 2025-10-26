package com.techhome.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var btnCheckout: Button
    private lateinit var layoutEmptyCart: LinearLayout
    private lateinit var btnBackCart: ImageButton

    private lateinit var cartAdapter: CartAdapter
    private val cartRepository = CartRepository()
    private val shippingCost = 5.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // Inicializar views
        rvCartItems = findViewById(R.id.rvCartItems)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvShipping = findViewById(R.id.tvShipping)
        tvTotal = findViewById(R.id.tvTotal)
        btnCheckout = findViewById(R.id.btnCheckout)
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart)
        btnBackCart = findViewById(R.id.btnBackCart)

        // Configurar RecyclerView
        rvCartItems.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(mutableListOf(),
            onQuantityChange = { item, newQuantity ->
                cartRepository.updateQuantity(
                    getUserId(), item.productSku, newQuantity,
                    onSuccess = { loadCartItems() },
                    onError = { showError(it) }
                )
            },
            onRemove = { item ->
                cartRepository.removeFromCart(
                    getUserId(), item.productSku,
                    onSuccess = { loadCartItems() },
                    onError = { showError(it) }
                )
            }
        )
        rvCartItems.adapter = cartAdapter

        // Cargar productos
        loadCartItems()

        // Botón finalizar compra
        btnCheckout.setOnClickListener {
            Toast.makeText(this, "Compra finalizada 🛒", Toast.LENGTH_SHORT).show()
        }

        // Botón volver
        btnBackCart.setOnClickListener { finish() }
    }

    private fun getUserId(): String {
        // Retorna UID de Firebase si está logueado
        return FirebaseAuth.getInstance().currentUser?.uid ?: "demo_user"
    }

    private fun loadCartItems() {
        cartRepository.getCartItems(
            getUserId(),
            onSuccess = { items ->
                if (items.isEmpty()) {
                    layoutEmptyCart.visibility = View.VISIBLE
                    rvCartItems.visibility = View.GONE
                } else {
                    layoutEmptyCart.visibility = View.GONE
                    rvCartItems.visibility = View.VISIBLE
                    cartAdapter.updateItems(items)
                }
                updateSummary(items)
            },
            onError = { showError(it) }
        )
    }

    private fun updateSummary(items: List<CartItem>) {
        val subtotal = cartRepository.calculateCartTotal(items)
        val shipping = if (items.isEmpty()) 0.0 else shippingCost
        val total = subtotal + shipping

        tvSubtotal.text = "$${"%.2f".format(subtotal)}"
        tvShipping.text = "$${"%.2f".format(shipping)}"
        tvTotal.text = "$${"%.2f".format(total)}"
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }
}
