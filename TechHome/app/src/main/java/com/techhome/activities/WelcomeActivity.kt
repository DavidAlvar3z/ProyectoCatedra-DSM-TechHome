package com.techhome.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.techhome.R
import com.techhome.adapters.CategoryAdapter
import com.techhome.models.Category

class WelcomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var rvCategories: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter

    companion object {
        // Categorías originales
        const val CATEGORY_CELL_PHONES = "abcat0800000"
        const val CATEGORY_LAPTOPS = "abcat0502000"
        const val CATEGORY_AUDIO = "abcat0200000"
        const val CATEGORY_SMARTWATCHES = "pcmcat748302045979"

        // Nuevas categorías
        const val CATEGORY_TABLETS = "pcmcat209000050006"
        const val CATEGORY_CAMERAS = "abcat0401000"
        const val CATEGORY_TV = "abcat0101000"
        const val CATEGORY_GAMING = "abcat0700000"
        const val CATEGORY_COMPUTER_ACCESSORIES = "abcat0515000"
        const val CATEGORY_WEARABLE_TECH = "pcmcat332000050000"
        const val CATEGORY_SMART_HOME = "pcmcat242800050021"
        const val CATEGORY_HEADPHONES = "abcat0204000"
        const val CATEGORY_SPEAKERS = "pcmcat310200050004"
        const val CATEGORY_MONITORS = "abcat0509000"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        auth = FirebaseAuth.getInstance()

        setupUI()
        setupBottomNavigation()
        setupCategories()
    }

    private fun setupUI() {
        val user = auth.currentUser
        user?.let {
            findViewById<TextView>(R.id.tvWelcomeMessage)?.text = "Bienvenido a HomeTech"
            findViewById<TextView>(R.id.tvUserEmail)?.text = it.email
        }
    }

    private fun setupCategories() {
        rvCategories = findViewById(R.id.rvCategories)
        rvCategories.layoutManager = GridLayoutManager(this, 2)

        val categories = listOf(
            Category("Teléfonos", CATEGORY_CELL_PHONES, R.drawable.ic_smartphone, R.drawable.gradient_category_1),
            Category("Laptops", CATEGORY_LAPTOPS, R.drawable.ic_laptop, R.drawable.gradient_category_2),
            Category("Audio", CATEGORY_AUDIO, R.drawable.ic_headset, R.drawable.gradient_category_3),
            Category("Smartwatches", CATEGORY_SMARTWATCHES, R.drawable.ic_watch, R.drawable.gradient_category_4),
            Category("Tablets", CATEGORY_TABLETS, R.drawable.ic_tablet, R.drawable.gradient_category_1),
            Category("Cámaras", CATEGORY_CAMERAS, R.drawable.ic_camera, R.drawable.gradient_category_2),
            Category("Televisores", CATEGORY_TV, R.drawable.ic_tv, R.drawable.gradient_category_3),
            Category("Gaming", CATEGORY_GAMING, R.drawable.ic_gaming, R.drawable.gradient_category_4),
            Category("Accesorios PC", CATEGORY_COMPUTER_ACCESSORIES, R.drawable.ic_mouse, R.drawable.gradient_category_1),
            Category("Tech Wearable", CATEGORY_WEARABLE_TECH, R.drawable.ic_watch, R.drawable.gradient_category_2),
            Category("Smart Home", CATEGORY_SMART_HOME, R.drawable.ic_home_smart, R.drawable.gradient_category_3),
            Category("Audífonos", CATEGORY_HEADPHONES, R.drawable.ic_headphones, R.drawable.gradient_category_4),
            Category("Bocinas", CATEGORY_SPEAKERS, R.drawable.ic_speaker, R.drawable.gradient_category_1),
            Category("Monitores", CATEGORY_MONITORS, R.drawable.ic_monitor, R.drawable.gradient_category_2)
        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            openCategory(category.id, category.name)
        }
        rvCategories.adapter = categoryAdapter
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
                    R.id.nav_search -> {
                        startActivity(Intent(this, SearchActivity::class.java))
                        true
                    }
                    R.id.nav_profile -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        true
                    }
                    R.id.nav_cart -> {
                        startActivity(Intent(this, CartActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
        }
    }
}