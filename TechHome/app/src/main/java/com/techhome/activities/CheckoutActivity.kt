package com.techhome.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.techhome.R
import com.techhome.adapters.CheckoutItemAdapter
import com.techhome.models.*
import com.techhome.repository.AddressRepository
import com.techhome.repository.CartRepository
import com.techhome.repository.OrderRepository
import com.techhome.repository.PaymentRepository
import java.text.NumberFormat
import java.util.*

class CheckoutActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var cartRepository: CartRepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var addressRepository: AddressRepository
    private lateinit var paymentRepository: PaymentRepository

    // Views principales
    private lateinit var btnBack: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var rvCheckoutItems: RecyclerView

    // Dirección de envío
    private lateinit var cvShippingAddress: CardView
    private lateinit var tvShippingName: TextView
    private lateinit var tvShippingAddress: TextView
    private lateinit var tvShippingPhone: TextView
    private lateinit var btnChangeAddress: MaterialButton
    private lateinit var layoutNoAddress: LinearLayout
    private lateinit var btnAddAddress: MaterialButton

    // Método de pago
    private lateinit var cvPaymentMethod: CardView
    private lateinit var tvPaymentName: TextView
    private lateinit var tvPaymentDetails: TextView
    private lateinit var btnChangePayment: MaterialButton
    private lateinit var layoutNoPayment: LinearLayout
    private lateinit var btnAddPayment: MaterialButton

    // Resumen de orden
    private lateinit var tvOrderSubtotal: TextView
    private lateinit var tvOrderShipping: TextView
    private lateinit var tvOrderTax: TextView
    private lateinit var tvOrderTotal: TextView

    // Botón de pago
    private lateinit var btnPlaceOrder: MaterialButton

    // Data
    private lateinit var checkoutAdapter: CheckoutItemAdapter
    private var cartItems: List<CartItem> = emptyList()
    private var selectedAddress: ShippingAddress? = null
    private var selectedPayment: PaymentMethod? = null

    private val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    private val shippingCost = 5.0
    private val taxRate = 0.13 // 13% IVA

    companion object {
        private const val TAG = "CheckoutActivity"
    }

    // Launcher para agregar dirección
    private val addAddressLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "✅ Dirección agregada, recargando...")
            loadUserAddresses()
        }
    }

    // Launcher para agregar método de pago
    private val addPaymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "✅ Método de pago agregado, recargando...")
            loadUserPayments()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        Log.d(TAG, "🛒 onCreate() - Iniciando CheckoutActivity")

        auth = FirebaseAuth.getInstance()
        cartRepository = CartRepository()
        orderRepository = OrderRepository()
        addressRepository = AddressRepository()
        paymentRepository = PaymentRepository()

        initViews()
        setupRecyclerView()
        setupListeners()
        loadCartItems()
        loadUserAddresses()
        loadUserPayments()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        rvCheckoutItems = findViewById(R.id.rvCheckoutItems)

        // Dirección
        cvShippingAddress = findViewById(R.id.cvShippingAddress)
        tvShippingName = findViewById(R.id.tvShippingName)
        tvShippingAddress = findViewById(R.id.tvShippingAddress)
        tvShippingPhone = findViewById(R.id.tvShippingPhone)
        btnChangeAddress = findViewById(R.id.btnChangeAddress)
        layoutNoAddress = findViewById(R.id.layoutNoAddress)
        btnAddAddress = findViewById(R.id.btnAddAddress)

        // Pago
        cvPaymentMethod = findViewById(R.id.cvPaymentMethod)
        tvPaymentName = findViewById(R.id.tvPaymentName)
        tvPaymentDetails = findViewById(R.id.tvPaymentDetails)
        btnChangePayment = findViewById(R.id.btnChangePayment)
        layoutNoPayment = findViewById(R.id.layoutNoPayment)
        btnAddPayment = findViewById(R.id.btnAddPayment)

        // Resumen
        tvOrderSubtotal = findViewById(R.id.tvOrderSubtotal)
        tvOrderShipping = findViewById(R.id.tvOrderShipping)
        tvOrderTax = findViewById(R.id.tvOrderTax)
        tvOrderTotal = findViewById(R.id.tvOrderTotal)

        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
    }

    private fun setupRecyclerView() {
        rvCheckoutItems.layoutManager = LinearLayoutManager(this)
        checkoutAdapter = CheckoutItemAdapter(emptyList())
        rvCheckoutItems.adapter = checkoutAdapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnAddAddress.setOnClickListener {
            openAddAddress()
        }

        btnChangeAddress.setOnClickListener {
            openAddAddress()
        }

        btnAddPayment.setOnClickListener {
            openAddPayment()
        }

        btnChangePayment.setOnClickListener {
            openAddPayment()
        }

        btnPlaceOrder.setOnClickListener {
            validateAndPlaceOrder()
        }
    }

    private fun openAddAddress() {
        val intent = Intent(this, AddAddressActivity::class.java)
        addAddressLauncher.launch(intent)
    }

    private fun openAddPayment() {
        val intent = Intent(this, AddPaymentActivity::class.java)
        addPaymentLauncher.launch(intent)
    }

    private fun loadCartItems() {
        showLoading()

        val userId = getUserId()
        cartRepository.getCartItems(
            userId,
            onSuccess = { items ->
                hideLoading()
                if (items.isEmpty()) {
                    showError("Tu carrito está vacío")
                    finish()
                } else {
                    cartItems = items
                    checkoutAdapter.updateItems(items)
                    updateOrderSummary()
                }
            },
            onError = { error ->
                hideLoading()
                showError("Error al cargar items: $error")
            }
        )
    }

    private fun loadUserAddresses() {
        val userId = getUserId()
        addressRepository.getUserAddresses(
            userId,
            onSuccess = { addresses ->
                if (addresses.isNotEmpty()) {
                    // Seleccionar la dirección por defecto o la primera
                    selectedAddress = addresses.firstOrNull { it.isDefault } ?: addresses.first()
                    showAddress(selectedAddress!!)
                } else {
                    showNoAddress()
                }
            },
            onError = { error ->
                Log.e(TAG, "Error al cargar direcciones: $error")
                showNoAddress()
            }
        )
    }

    private fun loadUserPayments() {
        val userId = getUserId()
        paymentRepository.getUserPayments(
            userId,
            onSuccess = { payments ->
                if (payments.isNotEmpty()) {
                    // Seleccionar el método por defecto o el primero
                    selectedPayment = payments.firstOrNull { it.isDefault } ?: payments.first()
                    showPayment(selectedPayment!!)
                } else {
                    showNoPayment()
                }
            },
            onError = { error ->
                Log.e(TAG, "Error al cargar métodos de pago: $error")
                showNoPayment()
            }
        )
    }

    private fun updateOrderSummary() {
        val subtotal = cartRepository.calculateCartTotal(cartItems)
        val shipping = if (cartItems.isEmpty()) 0.0 else shippingCost
        val tax = subtotal * taxRate
        val total = subtotal + shipping + tax

        tvOrderSubtotal.text = formatter.format(subtotal)
        tvOrderShipping.text = formatter.format(shipping)
        tvOrderTax.text = formatter.format(tax)
        tvOrderTotal.text = formatter.format(total)
    }

    private fun showAddress(address: ShippingAddress) {
        layoutNoAddress.visibility = View.GONE
        cvShippingAddress.visibility = View.VISIBLE
        btnChangeAddress.visibility = View.VISIBLE

        tvShippingName.text = address.fullName
        tvShippingAddress.text = address.getFullAddress()
        tvShippingPhone.text = "📞 ${address.phoneNumber}"
    }

    private fun showNoAddress() {
        layoutNoAddress.visibility = View.VISIBLE
        cvShippingAddress.visibility = View.GONE
        btnChangeAddress.visibility = View.GONE
    }

    private fun showPayment(payment: PaymentMethod) {
        layoutNoPayment.visibility = View.GONE
        cvPaymentMethod.visibility = View.VISIBLE
        btnChangePayment.visibility = View.VISIBLE

        tvPaymentName.text = payment.getDisplayName()
        tvPaymentDetails.text = when (payment.type) {
            PaymentType.CREDIT_CARD, PaymentType.DEBIT_CARD ->
                "Expira: ${payment.expiryMonth}/${payment.expiryYear}"
            PaymentType.PAYPAL -> "Cuenta verificada"
            PaymentType.CASH_ON_DELIVERY -> "Pago al recibir el pedido"
        }
    }

    private fun showNoPayment() {
        layoutNoPayment.visibility = View.VISIBLE
        cvPaymentMethod.visibility = View.GONE
        btnChangePayment.visibility = View.GONE
    }

    private fun validateAndPlaceOrder() {
        // Validar dirección
        if (selectedAddress == null) {
            showError("Por favor selecciona una dirección de envío")
            return
        }

        if (!selectedAddress!!.isComplete()) {
            showError("La dirección de envío está incompleta")
            return
        }

        // Validar método de pago
        if (selectedPayment == null) {
            showError("Por favor selecciona un método de pago")
            return
        }

        if (!selectedPayment!!.isComplete()) {
            showError("Los datos de pago están incompletos")
            return
        }

        // Validar carrito
        if (cartItems.isEmpty()) {
            showError("Tu carrito está vacío")
            return
        }

        // Mostrar confirmación
        showOrderConfirmation()
    }

    private fun showOrderConfirmation() {
        val subtotal = cartRepository.calculateCartTotal(cartItems)
        val shipping = shippingCost
        val tax = subtotal * taxRate
        val total = subtotal + shipping + tax

        AlertDialog.Builder(this)
            .setTitle("Confirmar Pedido")
            .setMessage(
                "¿Deseas confirmar tu pedido?\n\n" +
                        "Total a pagar: ${formatter.format(total)}\n" +
                        "Dirección: ${selectedAddress?.city}\n" +
                        "Pago: ${selectedPayment?.getDisplayName()}"
            )
            .setPositiveButton("Confirmar") { _, _ ->
                placeOrder()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun placeOrder() {
        showLoading()

        val subtotal = cartRepository.calculateCartTotal(cartItems)
        val shipping = shippingCost
        val tax = subtotal * taxRate
        val total = subtotal + shipping + tax

        val order = Order(
            orderId = UUID.randomUUID().toString(),
            userId = getUserId(),
            items = cartItems,
            shippingAddress = selectedAddress,
            paymentMethod = selectedPayment,
            subtotal = subtotal,
            shippingCost = shipping,
            tax = tax,
            total = total,
            status = OrderStatus.PENDING
        )

        orderRepository.createOrder(
            order,
            onSuccess = {
                hideLoading()
                clearCart()
                showOrderSuccess(order.orderId)
            },
            onError = { error ->
                hideLoading()
                showError("Error al procesar el pedido: $error")
            }
        )
    }

    private fun clearCart() {
        cartRepository.clearCart(
            getUserId(),
            onSuccess = {
                Log.d(TAG, "✅ Carrito vaciado")
            },
            onError = {
                Log.e(TAG, "❌ Error al vaciar carrito")
            }
        )
    }

    private fun showOrderSuccess(orderId: String) {
        AlertDialog.Builder(this)
            .setTitle("¡Pedido Exitoso! 🎉")
            .setMessage(
                "Tu pedido ha sido procesado exitosamente.\n\n" +
                        "Número de orden: #${orderId.take(8).uppercase()}\n\n" +
                        "Recibirás un correo con los detalles."
            )
            .setPositiveButton("Ir al Inicio") { _, _ ->
                goToHome()
            }
            .setCancelable(false)
            .show()
    }

    private fun goToHome() {
        val intent = Intent(this, WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        btnPlaceOrder.isEnabled = false
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
        btnPlaceOrder.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun getUserId(): String {
        return auth.currentUser?.uid ?: "demo_user"
    }
}