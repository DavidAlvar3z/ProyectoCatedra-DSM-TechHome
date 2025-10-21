package com.techhome.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.techhome.R

class OptionsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "OptionsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        configureGoogleSignIn()
        setupListeners()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnRegisterEmail).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<View>(R.id.btnGoogleSignIn).setOnClickListener {
            signInWithGoogle()
        }

        findViewById<View>(R.id.btnHaveAccount).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.btnClose).setOnClickListener {
            finish()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "firebaseAuthWithGoogle: ${account.id}")
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Error al iniciar sesión con Google: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle: ${account.id}")

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    // Guardar información del usuario en Firestore
                    user?.let { firebaseUser ->
                        val userData = hashMapOf(
                            "uid" to firebaseUser.uid,
                            "nombre" to (account.givenName ?: ""),
                            "apellido" to (account.familyName ?: ""),
                            "email" to (firebaseUser.email ?: ""),
                            "photoUrl" to (account.photoUrl?.toString() ?: ""),
                            "fechaRegistro" to System.currentTimeMillis(),
                            "proveedor" to "google"
                        )

                        db.collection("users")
                            .document(firebaseUser.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "¡Bienvenido ${account.givenName}!", Toast.LENGTH_SHORT).show()
                                goToWelcome()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error al guardar usuario: ${e.message}")
                                // Aún así ir a Welcome
                                goToWelcome()
                            }
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Autenticación fallida: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun goToWelcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}