package com.techhome.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.techhome.R
import com.techhome.network.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var products: List<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProductImage: ImageView = view.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = view.findViewById(R.id.tvProductPrice)
        val tvRegularPrice: TextView = view.findViewById(R.id.tvRegularPrice)

        fun bind(product: Product) {
            tvProductName.text = product.name

            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            tvProductPrice.text = formatter.format(product.salePrice)

            if (product.salePrice < product.regularPrice) {
                tvRegularPrice.visibility = View.VISIBLE
                tvRegularPrice.text = formatter.format(product.regularPrice)
                tvRegularPrice.paintFlags = tvRegularPrice.paintFlags or
                        android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvRegularPrice.visibility = View.GONE
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
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}