package com.techhome.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.techhome.R
import com.techhome.models.CartItem
import java.text.NumberFormat
import java.util.*

class CheckoutItemAdapter(
    private var items: List<CartItem>
) : RecyclerView.Adapter<CheckoutItemAdapter.ViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProduct: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checkout_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvName.text = item.productName
        holder.tvQuantity.text = "Cantidad: ${item.quantity}"
        holder.tvPrice.text = formatter.format(item.getSubtotal())

        Glide.with(holder.itemView.context)
            .load(item.productImage)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(holder.ivProduct)
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}