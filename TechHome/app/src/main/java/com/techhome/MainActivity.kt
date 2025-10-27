package com.techhome.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.techhome.R

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Esperar 2 segundos y verificar si hay usuario logueado
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, 2000)
    }

    private fun checkUserSession() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Usuario ya logueado, ir a WelcomeActivity
            startActivity(Intent(this, WelcomeActivity::class.java))
        } else {
            // No hay usuario, ir a OptionsActivity
            startActivity(Intent(this, OptionsActivity::class.java))
        }
        finish()
    }
}