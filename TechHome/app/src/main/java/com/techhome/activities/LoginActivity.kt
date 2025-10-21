package com.techhome.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.techhome.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoogleSignIn: MaterialButton
    private lateinit var btnNeedAccount: MaterialButton
    private lateinit var progressBar: ProgressBar

    // Launcher para Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Log.e("LoginActivity", "Google sign in failed: ${e.statusCode}")
            Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            showLoading(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        btnNeedAccount = findViewById(R.id.btnNeedAccount)
        progressBar = findViewById(R.id.progressBar)

        // Verificar si ya está logueado
        if (auth.currentUser != null) {
            goToWelcome()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            loginUser(email, password)
        }

        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        btnNeedAccount.setOnClickListener {
            startActivity(Intent(this, OptionsActivity::class.java))
        }

        findViewById<View>(R.id.btnClose).setOnClickListener {
            finish()
        }
    }

    private fun loginUser(email: String, password: String) {
        // Validaciones
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Login con Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                    goToWelcome()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun signInWithGoogle() {
        showLoading(true)
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        Log.d("LoginActivity", "firebaseAuthWithGoogle: ${account?.id}")

        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "signInWithCredential:success")
                    val user = auth.currentUser

                    // Guardar datos adicionales en Firestore si es nuevo usuario
                    user?.let {
                        val userData = hashMapOf(
                            "uid" to it.uid,
                            "nombre" to (it.displayName?.split(" ")?.getOrNull(0) ?: ""),
                            "apellido" to (it.displayName?.split(" ")?.getOrNull(1) ?: ""),
                            "email" to (it.email ?: ""),
                            "photoUrl" to (it.photoUrl?.toString() ?: ""),
                            "fechaRegistro" to System.currentTimeMillis()
                        )

                        db.collection("users")
                            .document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                showLoading(false)
                                Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                                goToWelcome()
                            }
                            .addOnFailureListener { e ->
                                Log.e("LoginActivity", "Error guardando usuario: ${e.message}")
                                showLoading(false)
                                goToWelcome() // Ir de todos modos aunque falle guardar
                            }
                    }
                } else {
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    showLoading(false)
                    Toast.makeText(this, "Error al autenticar con Google", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
        btnGoogleSignIn.isEnabled = !show
        btnNeedAccount.isEnabled = !show
    }

    private fun goToWelcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}