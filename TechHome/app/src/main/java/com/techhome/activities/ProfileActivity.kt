package com.techhome.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.techhome.R
import java.util.UUID

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var ivProfilePhoto: ImageView
    private lateinit var fabChangePhoto: FloatingActionButton
    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellido: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etEdad: TextInputEditText
    private lateinit var actvSexo: AutoCompleteTextView
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etBiografia: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null
    private var currentPhotoUrl: String = ""

    // Launcher para seleccionar imagen
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let {
                ivProfilePhoto.setImageURI(it)
                Toast.makeText(this, "Foto seleccionada. Guarda los cambios.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        initViews()
        setupSexoDropdown()
        setupListeners()
        loadUserData()
    }

    private fun initViews() {
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)
        fabChangePhoto = findViewById(R.id.fabChangePhoto)
        etNombre = findViewById(R.id.etNombre)
        etApellido = findViewById(R.id.etApellido)
        etEmail = findViewById(R.id.etEmail)
        etEdad = findViewById(R.id.etEdad)
        actvSexo = findViewById(R.id.actvSexo)
        etTelefono = findViewById(R.id.etTelefono)
        etBiografia = findViewById(R.id.etBiografia)
        btnSave = findViewById(R.id.btnSave)
        btnLogout = findViewById(R.id.btnLogout)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupSexoDropdown() {
        val sexos = arrayOf("Masculino", "Femenino", "Otro", "Prefiero no decirlo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sexos)
        actvSexo.setAdapter(adapter)
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        fabChangePhoto.setOnClickListener {
            openImagePicker()
        }

        btnSave.setOnClickListener {
            saveUserData()
        }

        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return

        showLoading(true)

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                showLoading(false)

                if (document.exists()) {
                    etNombre.setText(document.getString("nombre") ?: "")
                    etApellido.setText(document.getString("apellido") ?: "")
                    etEmail.setText(document.getString("email") ?: user.email)

                    val edad = document.getLong("edad")?.toInt()
                    if (edad != null && edad > 0) {
                        etEdad.setText(edad.toString())
                    }

                    actvSexo.setText(document.getString("sexo") ?: "", false)
                    etTelefono.setText(document.getString("telefono") ?: "")
                    etBiografia.setText(document.getString("biografia") ?: "")

                    currentPhotoUrl = document.getString("photoUrl") ?: ""
                    // TODO: Cargar foto con Glide o Picasso si existe

                    Log.d("ProfileActivity", "Datos cargados correctamente")
                } else {
                    Log.w("ProfileActivity", "Documento no existe, creando datos por defecto")
                    etEmail.setText(user.email)
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e("ProfileActivity", "Error al cargar datos: ${e.message}")
                Toast.makeText(this, "Error al cargar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData() {
        val user = auth.currentUser ?: return

        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()
        val edadStr = etEdad.text.toString().trim()
        val sexo = actvSexo.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val biografia = etBiografia.text.toString().trim()

        if (nombre.isEmpty() || apellido.isEmpty()) {
            Toast.makeText(this, "Nombre y apellido son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Si hay una imagen seleccionada, subirla primero
        if (selectedImageUri != null) {
            uploadImage(user.uid) { photoUrl ->
                saveToFirestore(user.uid, nombre, apellido, edadStr, sexo, telefono, biografia, photoUrl)
            }
        } else {
            saveToFirestore(user.uid, nombre, apellido, edadStr, sexo, telefono, biografia, currentPhotoUrl)
        }
    }

    private fun uploadImage(uid: String, onSuccess: (String) -> Unit) {
        val imageRef = storage.reference
            .child("profile_photos")
            .child("$uid/${UUID.randomUUID()}.jpg")

        selectedImageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        Log.d("ProfileActivity", "Imagen subida: $downloadUri")
                        onSuccess(downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    Log.e("ProfileActivity", "Error al subir imagen: ${e.message}")
                    Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveToFirestore(
        uid: String,
        nombre: String,
        apellido: String,
        edadStr: String,
        sexo: String,
        telefono: String,
        biografia: String,
        photoUrl: String
    ) {
        val edad = edadStr.toIntOrNull() ?: 0

        val userData = hashMapOf(
            "uid" to uid,
            "nombre" to nombre,
            "apellido" to apellido,
            "email" to (auth.currentUser?.email ?: ""),
            "edad" to edad,
            "sexo" to sexo,
            "telefono" to telefono,
            "biografia" to biografia,
            "photoUrl" to photoUrl,
            "fechaActualizacion" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                showLoading(false)
                Log.d("ProfileActivity", "Perfil actualizado correctamente")
                Toast.makeText(this, "✅ Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                currentPhotoUrl = photoUrl
                selectedImageUri = null
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e("ProfileActivity", "Error al guardar: ${e.message}")
                Toast.makeText(this, "❌ Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSave.isEnabled = !show
        btnLogout.isEnabled = !show
        fabChangePhoto.isEnabled = !show
    }
}