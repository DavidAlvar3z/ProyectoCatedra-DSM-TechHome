package com.techhome.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.techhome.R

class WelcomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Constantes de categorías de Best Buy
    companion object {
        private const val CATEGORY_CELL_PHONES = "abcat0800000"
        private const val CATEGORY_LAPTOPS = "abcat0502000"
        private const val CATEGORY_AUDIO = "abcat0200000"
        private const val CATEGORY_SMARTWATCHES = "pcmcat748302045979"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        auth = FirebaseAuth.getInstance()

        setupUI()
        setupBottomNavigation()
        setupCategoryCards()
    }

    private fun setupUI() {
        val user = auth.currentUser
        user?.let {
            findViewById<TextView>(R.id.tvWelcomeMessage)?.text = "Bienvenido a HomeTech"
            findViewById<TextView>(R.id.tvUserEmail)?.text = it.email
        }
    }

    private fun setupCategoryCards() {
        // Teléfonos
        findViewById<CardView>(R.id.cardPhones)?.setOnClickListener {
            openCategory(CATEGORY_CELL_PHONES, "Teléfonos")
        }

        // Laptops
        findViewById<CardView>(R.id.cardLaptops)?.setOnClickListener {
            openCategory(CATEGORY_LAPTOPS, "Laptops")
        }

        // Audio
        findViewById<CardView>(R.id.cardAudio)?.setOnClickListener {
            openCategory(CATEGORY_AUDIO, "Audio")
        }

        // Relojes Inteligentes
        findViewById<CardView>(R.id.cardSmartwatches)?.setOnClickListener {
            openCategory(CATEGORY_SMARTWATCHES, "Relojes Inteligentes")
        }
    }

    private fun openCategory(categoryId: String, categoryName: String) {
        val intent = Intent(this, ProductsActivity::class.java).apply {
            putExtra(ProductsActivity.EXTRA_CATEGORY_ID, categoryId)
            putExtra(ProductsActivity.EXTRA_CATEGORY_NAME, categoryName)
        }
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.llBottomNav)?.let { bottomNav ->
            bottomNav.selectedItemId = R.id.nav_home

            bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> true
                    R.id.nav_profile -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        true
                    }
                    R.id.nav_cart -> {
                        // TODO: Implementar carrito
                        true
                    }
                    R.id.nav_more -> {
                        // TODO: Implementar menú
                        true
                    }
                    else -> false
                }
            }
        }
    }
}