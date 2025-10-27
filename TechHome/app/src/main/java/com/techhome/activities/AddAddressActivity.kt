package com.techhome.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.techhome.R
import com.techhome.models.ShippingAddress
import com.techhome.repository.AddressRepository
import java.util.*

class AddAddressActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var addressRepository: AddressRepository

    // Views
    private lateinit var btnBack: ImageButton
    private lateinit var etFullName: TextInputEditText
    private lateinit var etPhoneNumber: TextInputEditText
    private lateinit var etAddressLine1: TextInputEditText
    private lateinit var etAddressLine2: TextInputEditText
    private lateinit var etCity: TextInputEditText
    private lateinit var actvState: MaterialAutoCompleteTextView
    private lateinit var etZipCode: TextInputEditText
    private lateinit var btnSaveAddress: MaterialButton
    private lateinit var progressBar: ProgressBar

    companion object {
        private const val TAG = "AddAddressActivity"
        const val EXTRA_ADDRESS_ID = "address_id"
    }

    // Departamentos de El Salvador
    private val states = listOf(
        "Ahuachapán", "Cabañas", "Chalatenango", "Cuscatlán",
        "La Libertad", "La Paz", "La Unión", "Morazán",
        "San Miguel", "San Salvador", "San Vicente", "Santa Ana",
        "Sonsonate", "Usulután"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_address)

        Log.d(TAG, "🎬 onCreate() - Iniciando AddAddressActivity")

        auth = FirebaseAuth.getInstance()
        addressRepository = AddressRepository()

        initViews()
        setupStateDropdown()
        setupListeners()
        setupPhoneFormatter() // ✅ NUEVO: Formatear teléfono
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etFullName = findViewById(R.id.etFullName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etAddressLine1 = findViewById(R.id.etAddressLine1)
        etAddressLine2 = findViewById(R.id.etAddressLine2)
        etCity = findViewById(R.id.etCity)
        actvState = findViewById(R.id.actvState)
        etZipCode = findViewById(R.id.etZipCode)
        btnSaveAddress = findViewById(R.id.btnSaveAddress)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupStateDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, states)
        actvState.setAdapter(adapter)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSaveAddress.setOnClickListener {
            validateAndSaveAddress()
        }
    }

    // ✅ NUEVO: Formatear teléfono automáticamente a 1111-1111
    private fun setupPhoneFormatter() {
        etPhoneNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                // Remover todo excepto números
                val digits = s.toString().replace(Regex("[^0-9]"), "")

                // Formatear a 1111-1111
                val formatted = when {
                    digits.length <= 4 -> digits
                    digits.length <= 8 -> "${digits.substring(0, 4)}-${digits.substring(4)}"
                    else -> "${digits.substring(0, 4)}-${digits.substring(4, 8)}"
                }

                // Actualizar el texto solo si cambió
                if (formatted != s.toString()) {
                    etPhoneNumber.setText(formatted)
                    etPhoneNumber.setSelection(formatted.length)
                }

                isFormatting = false
            }
        })
    }

    private fun validateAndSaveAddress() {
        // Obtener valores
        val fullName = etFullName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val addressLine1 = etAddressLine1.text.toString().trim()
        val addressLine2 = etAddressLine2.text.toString().trim()
        val city = etCity.text.toString().trim()
        val state = actvState.text.toString().trim()
        val zipCode = etZipCode.text.toString().trim()

        // Validaciones
        if (fullName.isEmpty()) {
            etFullName.error = "Ingresa tu nombre completo"
            etFullName.requestFocus()
            return
        }

        if (phoneNumber.isEmpty()) {
            etPhoneNumber.error = "Ingresa tu teléfono"
            etPhoneNumber.requestFocus()
            return
        }

        // Validar formato 1111-1111
        if (!phoneNumber.matches(Regex("^\\d{4}-\\d{4}$"))) {
            etPhoneNumber.error = "Formato inválido. Usa: 1111-1111"
            etPhoneNumber.requestFocus()
            return
        }

        if (addressLine1.isEmpty()) {
            etAddressLine1.error = "Ingresa tu dirección"
            etAddressLine1.requestFocus()
            return
        }

        if (city.isEmpty()) {
            etCity.error = "Ingresa tu ciudad"
            etCity.requestFocus()
            return
        }

        if (state.isEmpty()) {
            actvState.error = "Selecciona un departamento"
            actvState.requestFocus()
            return
        }

        if (zipCode.isEmpty()) {
            etZipCode.error = "Ingresa tu código postal"
            etZipCode.requestFocus()
            return
        }

        // Crear dirección
        saveAddress(fullName, phoneNumber, addressLine1, addressLine2, city, state, zipCode)
    }

    private fun saveAddress(
        fullName: String,
        phoneNumber: String,
        addressLine1: String,
        addressLine2: String,
        city: String,
        state: String,
        zipCode: String
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showError("Usuario no autenticado")
            return
        }

        showLoading()

        val address = ShippingAddress(
            addressId = UUID.randomUUID().toString(),
            userId = userId,
            fullName = fullName,
            phoneNumber = phoneNumber,
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            city = city,
            state = state,
            zipCode = zipCode,
            country = "El Salvador",
            isDefault = false
        )

        addressRepository.saveAddress(
            address = address,
            onSuccess = {
                hideLoading()
                Toast.makeText(this, "✅ Dirección guardada exitosamente", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            },
            onError = { error ->
                hideLoading()
                showError("Error al guardar dirección: $error")
            }
        )
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        btnSaveAddress.isEnabled = false
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
        btnSaveAddress.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}