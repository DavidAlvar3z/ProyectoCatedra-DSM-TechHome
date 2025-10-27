package com.techhome.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.techhome.R
import com.techhome.models.PaymentMethod
import com.techhome.models.PaymentType
import com.techhome.repository.PaymentRepository
import java.util.*

class AddPaymentActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var paymentRepository: PaymentRepository

    // Views
    private lateinit var btnBack: ImageButton
    private lateinit var rgPaymentType: RadioGroup
    private lateinit var rbCreditCard: RadioButton
    private lateinit var rbDebitCard: RadioButton
    private lateinit var rbPaypal: RadioButton
    private lateinit var rbCashOnDelivery: RadioButton

    // Card views
    private lateinit var layoutCardDetails: LinearLayout
    private lateinit var layoutPaypalDetails: LinearLayout

    // Card fields
    private lateinit var etCardHolderName: TextInputEditText
    private lateinit var etCardNumber: TextInputEditText
    private lateinit var etExpiryDate: TextInputEditText
    private lateinit var etCVV: TextInputEditText

    // PayPal fields
    private lateinit var etPaypalEmail: TextInputEditText

    private lateinit var btnSavePayment: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var selectedPaymentType = PaymentType.CREDIT_CARD

    companion object {
        private const val TAG = "AddPaymentActivity"
        const val EXTRA_PAYMENT_ID = "payment_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_payment)

        Log.d(TAG, "🎬 onCreate() - Iniciando AddPaymentActivity")

        auth = FirebaseAuth.getInstance()
        paymentRepository = PaymentRepository()

        initViews()
        setupListeners()
        showCardDetails() // Por defecto mostrar tarjeta
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        rgPaymentType = findViewById(R.id.rgPaymentType)
        rbCreditCard = findViewById(R.id.rbCreditCard)
        rbDebitCard = findViewById(R.id.rbDebitCard)
        rbPaypal = findViewById(R.id.rbPaypal)
        rbCashOnDelivery = findViewById(R.id.rbCashOnDelivery)

        layoutCardDetails = findViewById(R.id.layoutCardDetails)
        layoutPaypalDetails = findViewById(R.id.layoutPaypalDetails)

        etCardHolderName = findViewById(R.id.etCardHolderName)
        etCardNumber = findViewById(R.id.etCardNumber)
        etExpiryDate = findViewById(R.id.etExpiryDate)
        etCVV = findViewById(R.id.etCVV)

        etPaypalEmail = findViewById(R.id.etPaypalEmail)

        btnSavePayment = findViewById(R.id.btnSavePayment)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        rgPaymentType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCreditCard -> {
                    selectedPaymentType = PaymentType.CREDIT_CARD
                    showCardDetails()
                }
                R.id.rbDebitCard -> {
                    selectedPaymentType = PaymentType.DEBIT_CARD
                    showCardDetails()
                }
                R.id.rbPaypal -> {
                    selectedPaymentType = PaymentType.PAYPAL
                    showPaypalDetails()
                }
                R.id.rbCashOnDelivery -> {
                    selectedPaymentType = PaymentType.CASH_ON_DELIVERY
                    hideAllDetails()
                }
            }
        }

        // Formatear número de tarjeta
        etCardNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val text = s.toString().replace(" ", "")
                if (text.length <= 16) {
                    val formatted = text.chunked(4).joinToString(" ")
                    etCardNumber.setText(formatted)
                    etCardNumber.setSelection(formatted.length)
                }

                isFormatting = false
            }
        })

        // Formatear fecha de expiración
        etExpiryDate.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val text = s.toString().replace("/", "")
                if (text.length <= 4) {
                    val formatted = if (text.length >= 2) {
                        "${text.substring(0, 2)}/${text.substring(2)}"
                    } else {
                        text
                    }
                    etExpiryDate.setText(formatted)
                    etExpiryDate.setSelection(formatted.length)
                }

                isFormatting = false
            }
        })

        btnSavePayment.setOnClickListener {
            validateAndSavePayment()
        }
    }

    private fun showCardDetails() {
        layoutCardDetails.visibility = View.VISIBLE
        layoutPaypalDetails.visibility = View.GONE
    }

    private fun showPaypalDetails() {
        layoutCardDetails.visibility = View.GONE
        layoutPaypalDetails.visibility = View.VISIBLE
    }

    private fun hideAllDetails() {
        layoutCardDetails.visibility = View.GONE
        layoutPaypalDetails.visibility = View.GONE
    }

    private fun validateAndSavePayment() {
        when (selectedPaymentType) {
            PaymentType.CREDIT_CARD, PaymentType.DEBIT_CARD -> {
                validateAndSaveCard()
            }
            PaymentType.PAYPAL -> {
                validateAndSavePaypal()
            }
            PaymentType.CASH_ON_DELIVERY -> {
                saveCashOnDelivery()
            }
        }
    }

    private fun validateAndSaveCard() {
        val cardHolderName = etCardHolderName.text.toString().trim()
        val cardNumber = etCardNumber.text.toString().replace(" ", "").trim()
        val expiryDate = etExpiryDate.text.toString().trim()
        val cvv = etCVV.text.toString().trim()

        if (cardHolderName.isEmpty()) {
            etCardHolderName.error = "Ingresa el nombre del titular"
            etCardHolderName.requestFocus()
            return
        }

        if (cardNumber.isEmpty() || cardNumber.length != 16) {
            etCardNumber.error = "Ingresa un número de tarjeta válido (16 dígitos)"
            etCardNumber.requestFocus()
            return
        }

        if (!cardNumber.matches(Regex("^[0-9]+$"))) {
            etCardNumber.error = "Solo se permiten números"
            etCardNumber.requestFocus()
            return
        }

        if (expiryDate.isEmpty() || !expiryDate.matches(Regex("^\\d{2}/\\d{2}$"))) {
            etExpiryDate.error = "Formato inválido (MM/AA)"
            etExpiryDate.requestFocus()
            return
        }

        val parts = expiryDate.split("/")
        val month = parts[0].toIntOrNull() ?: 0
        if (month < 1 || month > 12) {
            etExpiryDate.error = "Mes inválido"
            etExpiryDate.requestFocus()
            return
        }

        if (cvv.isEmpty() || cvv.length < 3) {
            etCVV.error = "Ingresa el CVV (3-4 dígitos)"
            etCVV.requestFocus()
            return
        }

        savePaymentMethod(
            cardHolderName = cardHolderName,
            cardNumber = cardNumber,
            expiryMonth = parts[0],
            expiryYear = parts[1],
            cvv = cvv
        )
    }

    private fun validateAndSavePaypal() {
        val email = etPaypalEmail.text.toString().trim()

        if (email.isEmpty()) {
            etPaypalEmail.error = "Ingresa tu email de PayPal"
            etPaypalEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etPaypalEmail.error = "Email inválido"
            etPaypalEmail.requestFocus()
            return
        }

        savePaymentMethod(paypalEmail = email)
    }

    private fun saveCashOnDelivery() {
        savePaymentMethod()
    }

    private fun savePaymentMethod(
        cardHolderName: String = "",
        cardNumber: String = "",
        expiryMonth: String = "",
        expiryYear: String = "",
        cvv: String = "",
        paypalEmail: String = ""
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showError("Usuario no autenticado")
            return
        }

        showLoading()

        val payment = PaymentMethod(
            paymentId = UUID.randomUUID().toString(),
            userId = userId,
            type = selectedPaymentType,
            cardHolderName = cardHolderName,
            cardNumber = cardNumber,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            cvv = cvv,
            paypalEmail = paypalEmail,
            isDefault = false
        )

        paymentRepository.savePayment(
            payment = payment,
            onSuccess = {
                hideLoading()
                Toast.makeText(this, "✅ Método de pago guardado exitosamente", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            },
            onError = { error ->
                hideLoading()
                showError("Error al guardar método de pago: $error")
            }
        )
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        btnSavePayment.isEnabled = false
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
        btnSavePayment.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}