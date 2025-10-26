package com.techhome.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.techhome.R
import com.techhome.adapters.ProductLocalAdapter
import com.techhome.models.ProductLocal
import com.techhome.models.SearchFilter
import com.techhome.models.SortOption
import com.techhome.repository.ProductRepository

class SearchActivity : AppCompatActivity() {

    private lateinit var etSearch: TextInputEditText
    private lateinit var btnFilter: ImageButton
    private lateinit var rvResults: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoResults: View
    private lateinit var tvResultsCount: TextView

    private lateinit var productAdapter: ProductLocalAdapter
    private val repository = ProductRepository()

    private var allProducts = listOf<ProductLocal>()
    private var filteredProducts = listOf<ProductLocal>()
    private val currentFilter = SearchFilter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupSearchBar()
        setupRecyclerView()
        loadAllProducts()
    }

    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        btnFilter = findViewById(R.id.btnFilter)
        rvResults = findViewById(R.id.rvResults)
        progressBar = findViewById(R.id.progressBar)
        tvNoResults = findViewById(R.id.tvNoResults)
        tvResultsCount = findViewById(R.id.tvResultsCount)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun setupSearchBar() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentFilter.query = s?.toString() ?: ""
                applyFilters()
            }
        })
    }

    private fun setupRecyclerView() {
        rvResults.layoutManager = GridLayoutManager(this, 2)
        productAdapter = ProductLocalAdapter(emptyList()) { product ->
            openProductDetail(product)
        }
        rvResults.adapter = productAdapter
    }

    private fun loadAllProducts() {
        showLoading(true)

        repository.getAllProducts(
            onSuccess = { products ->
                showLoading(false)
                allProducts = products
                applyFilters()
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun applyFilters() {
        filteredProducts = allProducts.filter { product ->
            // Filtro de búsqueda por texto
            val matchesQuery = if (currentFilter.query.isEmpty()) {
                true
            } else {
                product.name.contains(currentFilter.query, ignoreCase = true) ||
                        product.brand.contains(currentFilter.query, ignoreCase = true) ||
                        product.category.contains(currentFilter.query, ignoreCase = true)
            }

            // Filtro de precio
            val matchesPrice = product.salePrice >= currentFilter.minPrice &&
                    product.salePrice <= currentFilter.maxPrice

            // Filtro de categoría
            val matchesCategory = if (currentFilter.selectedCategory.isEmpty()) {
                true
            } else {
                product.category.equals(currentFilter.selectedCategory, ignoreCase = true)
            }

            // Filtro de marca
            val matchesBrand = if (currentFilter.selectedBrand.isEmpty()) {
                true
            } else {
                product.brand.equals(currentFilter.selectedBrand, ignoreCase = true)
            }

            // Filtro de rating
            val matchesRating = product.rating >= currentFilter.minRating

            // Filtro de disponibilidad
            val matchesStock = if (currentFilter.inStockOnly) {
                product.stock > 0
            } else {
                true
            }

            matchesQuery && matchesPrice && matchesCategory && matchesBrand && matchesRating && matchesStock
        }

        // Aplicar ordenamiento
        filteredProducts = when (currentFilter.sortBy) {
            SortOption.NAME_ASC -> filteredProducts.sortedBy { it.name }
            SortOption.NAME_DESC -> filteredProducts.sortedByDescending { it.name }
            SortOption.PRICE_ASC -> filteredProducts.sortedBy { it.salePrice }
            SortOption.PRICE_DESC -> filteredProducts.sortedByDescending { it.salePrice }
            SortOption.RATING_DESC -> filteredProducts.sortedByDescending { it.rating }
            SortOption.NEWEST -> filteredProducts.sortedByDescending { it.sku }
        }

        updateUI()
    }

    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_filter, null)
        dialog.setContentView(view)

        // Referencias a las vistas
        val rangeSliderPrice = view.findViewById<RangeSlider>(R.id.rangeSliderPrice)
        val tvPriceRange = view.findViewById<TextView>(R.id.tvPriceRange)
        val chipGroupCategories = view.findViewById<ChipGroup>(R.id.chipGroupCategories)
        val chipGroupBrands = view.findViewById<ChipGroup>(R.id.chipGroupBrands)
        val chipGroupSort = view.findViewById<ChipGroup>(R.id.chipGroupSort)
        val sliderRating = view.findViewById<Slider>(R.id.sliderRating)
        val tvRatingValue = view.findViewById<TextView>(R.id.tvRatingValue)
        val switchInStock = view.findViewById<SwitchMaterial>(R.id.switchInStock)
        val btnReset = view.findViewById<MaterialButton>(R.id.btnReset)
        val btnApply = view.findViewById<MaterialButton>(R.id.btnApply)

        // Configurar Range Slider de precio
        rangeSliderPrice.values = listOf(
            currentFilter.minPrice.toFloat(),
            currentFilter.maxPrice.toFloat()
        )
        tvPriceRange.text = "$${currentFilter.minPrice.toInt()} - $${currentFilter.maxPrice.toInt()}"

        rangeSliderPrice.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            tvPriceRange.text = "$${values[0].toInt()} - $${values[1].toInt()}"
        }

        // Obtener categorías únicas
        val categories = allProducts.map { it.category }.distinct().sorted()
        chipGroupCategories.removeAllViews()
        categories.forEach { category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                isChecked = currentFilter.selectedCategory == category
            }
            chipGroupCategories.addView(chip)
        }

        // Obtener marcas únicas
        val brands = allProducts.map { it.brand }.distinct().sorted()
        chipGroupBrands.removeAllViews()
        brands.forEach { brand ->
            val chip = Chip(this).apply {
                text = brand
                isCheckable = true
                isChecked = currentFilter.selectedBrand == brand
            }
            chipGroupBrands.addView(chip)
        }

        // Configurar chips de ordenamiento
        setupSortChips(chipGroupSort)

        // Configurar slider de rating
        sliderRating.value = currentFilter.minRating.toFloat()
        tvRatingValue.text = "${currentFilter.minRating} ⭐"

        sliderRating.addOnChangeListener { _, value, _ ->
            tvRatingValue.text = "${String.format("%.1f", value)} ⭐"
        }

        // Configurar switch de stock
        switchInStock.isChecked = currentFilter.inStockOnly

        // Botón Reset
        btnReset.setOnClickListener {
            currentFilter.minPrice = 0.0
            currentFilter.maxPrice = 10000.0
            currentFilter.selectedCategory = ""
            currentFilter.selectedBrand = ""
            currentFilter.minRating = 0.0
            currentFilter.inStockOnly = false
            currentFilter.sortBy = SortOption.NAME_ASC

            rangeSliderPrice.values = listOf(0f, 10000f)
            chipGroupCategories.clearCheck()
            chipGroupBrands.clearCheck()
            chipGroupSort.check(R.id.chipSortNameAsc)
            sliderRating.value = 0f
            switchInStock.isChecked = false

            applyFilters()
            dialog.dismiss()
        }

        // Botón Apply
        btnApply.setOnClickListener {
            // Aplicar precio
            val priceValues = rangeSliderPrice.values
            currentFilter.minPrice = priceValues[0].toDouble()
            currentFilter.maxPrice = priceValues[1].toDouble()

            // Aplicar categoría
            val selectedCategoryChip = chipGroupCategories.findViewById<Chip>(
                chipGroupCategories.checkedChipId
            )
            currentFilter.selectedCategory = selectedCategoryChip?.text?.toString() ?: ""

            // Aplicar marca
            val selectedBrandChip = chipGroupBrands.findViewById<Chip>(
                chipGroupBrands.checkedChipId
            )
            currentFilter.selectedBrand = selectedBrandChip?.text?.toString() ?: ""

            // Aplicar rating
            currentFilter.minRating = sliderRating.value.toDouble()

            // Aplicar stock
            currentFilter.inStockOnly = switchInStock.isChecked

            // Aplicar ordenamiento
            currentFilter.sortBy = when (chipGroupSort.checkedChipId) {
                R.id.chipSortNameAsc -> SortOption.NAME_ASC
                R.id.chipSortNameDesc -> SortOption.NAME_DESC
                R.id.chipSortPriceAsc -> SortOption.PRICE_ASC
                R.id.chipSortPriceDesc -> SortOption.PRICE_DESC
                R.id.chipSortRating -> SortOption.RATING_DESC
                R.id.chipSortNewest -> SortOption.NEWEST
                else -> SortOption.NAME_ASC
            }

            applyFilters()
            dialog.dismiss()

            Toast.makeText(
                this,
                "Filtros aplicados: ${filteredProducts.size} resultados",
                Toast.LENGTH_SHORT
            ).show()
        }

        dialog.show()
    }

    private fun setupSortChips(chipGroup: ChipGroup) {
        chipGroup.removeAllViews()

        val sortOptions = listOf(
            R.id.chipSortNameAsc to "Nombre A-Z",
            R.id.chipSortNameDesc to "Nombre Z-A",
            R.id.chipSortPriceAsc to "Precio: Menor a Mayor",
            R.id.chipSortPriceDesc to "Precio: Mayor a Menor",
            R.id.chipSortRating to "Mejor Valorados",
            R.id.chipSortNewest to "Más Recientes"
        )

        sortOptions.forEach { (id, text) ->
            val chip = Chip(this).apply {
                this.id = id
                this.text = text
                isCheckable = true
                isChecked = when (currentFilter.sortBy) {
                    SortOption.NAME_ASC -> id == R.id.chipSortNameAsc
                    SortOption.NAME_DESC -> id == R.id.chipSortNameDesc
                    SortOption.PRICE_ASC -> id == R.id.chipSortPriceAsc
                    SortOption.PRICE_DESC -> id == R.id.chipSortPriceDesc
                    SortOption.RATING_DESC -> id == R.id.chipSortRating
                    SortOption.NEWEST -> id == R.id.chipSortNewest
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun updateUI() {
        if (filteredProducts.isEmpty()) {
            rvResults.visibility = View.GONE
            tvNoResults.visibility = View.VISIBLE
            tvResultsCount.text = "0 resultados"
        } else {
            rvResults.visibility = View.VISIBLE
            tvNoResults.visibility = View.GONE
            tvResultsCount.text = "${filteredProducts.size} resultado${if (filteredProducts.size != 1) "s" else ""}"
        }

        productAdapter.updateProducts(filteredProducts)
    }

    private fun openProductDetail(product: ProductLocal) {
        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra(ProductDetailActivity.EXTRA_PRODUCT_SKU, product.sku)
        }
        startActivity(intent)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}