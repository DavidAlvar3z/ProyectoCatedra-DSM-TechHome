package com.techhome.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.techhome.R
import com.techhome.utils.FirebaseHelper
import com.techhome.models.User
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
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
    private lateinit var progressContainer: View

    private var currentPhotoUri: Uri? = null
    private var photoUrl: String? = null
    private var currentPhotoPath: String? = null

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
        private const val TAG = "ProfileActivity"
    }

    // Activity Result Launchers
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                currentPhotoUri = uri
                displayPhoto(uri)
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoUri?.let { uri ->
                displayPhoto(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        initViews()
        setupListeners()
        setupSexDropdown()
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
        progressContainer = findViewById(R.id.progressContainer)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        fabChangePhoto.setOnClickListener {
            showPhotoOptionsDialog()
        }

        btnSave.setOnClickListener {
            saveUserProfile()
        }

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setupSexDropdown() {
        val sexOptions = arrayOf("Masculino", "Femenino", "Otro", "Prefiero no decir")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sexOptions)
        actvSexo.setAdapter(adapter)
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        showLoading(true)

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                showLoading(false)
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let { displayUserData(it) }
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayUserData(user: User) {
        etNombre.setText(user.nombre)
        etApellido.setText(user.apellido)
        etEmail.setText(user.email)
        etEdad.setText(user.edad?.toString() ?: "")
        actvSexo.setText(user.sexo, false)
        etTelefono.setText(user.telefono)
        etBiografia.setText(user.biografia)
        photoUrl = user.photoUrl

        // Cargar foto de perfil
        if (!user.photoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(user.photoUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(ivProfilePhoto)
        }
    }

    private fun showPhotoOptionsDialog() {
        val options = arrayOf(
            "Tomar foto",
            "Elegir de galería",
            "Ingresar URL de imagen",
            "Eliminar foto"
        )

        AlertDialog.Builder(this)
            .setTitle("Cambiar foto de perfil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndTakePhoto()
                    1 -> openGallery()
                    2 -> showUrlInputDialog()
                    3 -> removePhoto()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        photoFile?.let { file ->
            currentPhotoUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                file
            )
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
            takePictureLauncher.launch(takePictureIntent)
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = getExternalFilesDir(null)
            File.createTempFile(imageFileName, ".jpg", storageDir).also {
                currentPhotoPath = it.absolutePath
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al crear archivo: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun showUrlInputDialog() {
        val editText = TextInputEditText(this).apply {
            hint = "https://ejemplo.com/imagen.jpg"
            setPadding(50, 40, 50, 40)
        }

        AlertDialog.Builder(this)
            .setTitle("Ingresar URL de imagen")
            .setView(editText)
            .setPositiveButton("Cargar") { _, _ ->
                val url = editText.text.toString().trim()
                if (url.isNotEmpty()) {
                    photoUrl = url
                    currentPhotoUri = null // Limpiar URI local
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivProfilePhoto)
                    Toast.makeText(this, "URL de imagen cargada", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun removePhoto() {
        currentPhotoUri = null
        photoUrl = null
        ivProfilePhoto.setImageResource(R.drawable.ic_person)
        Toast.makeText(this, "Foto eliminada", Toast.LENGTH_SHORT).show()
    }

    private fun displayPhoto(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .circleCrop()
            .into(ivProfilePhoto)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserProfile() {
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

        val edad = edadStr.toIntOrNull()

        showLoading(true)

        // Si hay una imagen local, subirla primero
        if (currentPhotoUri != null) {
            uploadPhotoAndSaveProfile(nombre, apellido, edad, sexo, telefono, biografia)
        } else {
            // Si solo hay URL o no hay foto, guardar directamente
            saveProfileToFirestore(nombre, apellido, edad, sexo, telefono, biografia, photoUrl)
        }
    }

    private fun uploadPhotoAndSaveProfile(
        nombre: String,
        apellido: String,
        edad: Int?,
        sexo: String,
        telefono: String,
        biografia: String
    ) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("profile_photos/$userId.jpg")

        currentPhotoUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveProfileToFirestore(
                            nombre, apellido, edad, sexo, telefono, biografia,
                            downloadUri.toString()
                        )
                    }
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    Toast.makeText(this, "Error al subir foto: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileToFirestore(
        nombre: String,
        apellido: String,
        edad: Int?,
        sexo: String,
        telefono: String,
        biografia: String,
        photoUrl: String?
    ) {
        val userId = auth.currentUser?.uid ?: return

        val userUpdates = hashMapOf<String, Any?>(
            "nombre" to nombre,
            "apellido" to apellido,
            "edad" to edad,
            "sexo" to sexo,
            "telefono" to telefono,
            "biografia" to biografia,
            "photoUrl" to photoUrl
        )

        firestore.collection("users").document(userId)
            .update(userUpdates)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "✓ Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                FirebaseHelper.logout(this)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        progressContainer.visibility = if (show) View.VISIBLE else View.GONE
        btnSave.isEnabled = !show
        btnLogout.isEnabled = !show
    }
}