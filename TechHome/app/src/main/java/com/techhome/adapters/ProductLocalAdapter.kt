package com.techhome.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.techhome.R
import com.techhome.models.ProductLocal
import com.techhome.models.StockStatus
import java.text.NumberFormat
import java.util.Locale

class ProductLocalAdapter(
    private var products: List<ProductLocal>,
    private val onProductClick: (ProductLocal) -> Unit
) : RecyclerView.Adapter<ProductLocalAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cvProduct: CardView = view.findViewById(R.id.cvProduct)
        val ivProductImage: ImageView = view.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = view.findViewById(R.id.tvProductPrice)
        val tvRegularPrice: TextView = view.findViewById(R.id.tvRegularPrice)
        val chipDiscount: Chip = view.findViewById(R.id.chipDiscount)
        val chipStock: Chip = view.findViewById(R.id.chipStock)
        val tvRating: TextView = view.findViewById(R.id.tvRating)

        fun bind(product: ProductLocal) {
            tvProductName.text = product.name

            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            tvProductPrice.text = formatter.format(product.salePrice)

            val discount = product.getDiscountPercentage()
            if (discount > 0) {
                tvRegularPrice.visibility = View.VISIBLE
                tvRegularPrice.text = formatter.format(product.regularPrice)
                tvRegularPrice.paintFlags = tvRegularPrice.paintFlags or
                        android.graphics.Paint.STRIKE_THRU_TEXT_FLAG

                chipDiscount.visibility = View.VISIBLE
                chipDiscount.text = "-$discount%"
            } else {
                tvRegularPrice.visibility = View.GONE
                chipDiscount.visibility = View.GONE
            }

            tvRating.text = String.format("%.1f ⭐", product.rating)

            when (product.getStockStatus()) {
                StockStatus.IN_STOCK -> {
                    chipStock.visibility = View.VISIBLE
                    chipStock.text = "En Stock"
                    chipStock.setChipBackgroundColorResource(R.color.green_100)
                    chipStock.setTextColor(itemView.context.getColor(R.color.green_700))
                    cvProduct.alpha = 1f
                }
                StockStatus.LOW_STOCK -> {
                    chipStock.visibility = View.VISIBLE
                    chipStock.text = "¡Pocas unidades!"
                    chipStock.setChipBackgroundColorResource(R.color.orange_100)
                    chipStock.setTextColor(itemView.context.getColor(R.color.orange_700))
                    cvProduct.alpha = 1f
                }
                StockStatus.OUT_OF_STOCK -> {
                    chipStock.visibility = View.VISIBLE
                    chipStock.text = "Agotado"
                    chipStock.setChipBackgroundColorResource(R.color.red_100)
                    chipStock.setTextColor(itemView.context.getColor(R.color.red_700))
                    cvProduct.alpha = 0.6f
                }
            }

            Glide.with(itemView.context)
                .load(product.image)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(ivProductImage)

            itemView.setOnClickListener {
                onProductClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_local, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<ProductLocal>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
