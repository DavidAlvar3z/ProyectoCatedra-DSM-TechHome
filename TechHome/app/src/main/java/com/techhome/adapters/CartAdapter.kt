package com.techhome.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.techhome.R
import com.techhome.models.CartItem

class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    private val onQuantityChange: (CartItem, Int) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)
        val btnAdd: ImageButton = itemView.findViewById(R.id.btnAdd)
        val btnSubtract: ImageButton = itemView.findViewById(R.id.btnSubtract)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]

        holder.tvProductName.text = item.name
        holder.tvPrice.text = "$${"%.2f".format(item.salePrice)}"
        holder.tvQuantity.text = item.quantity.toString()
        holder.tvSubtotal.text = "$${"%.2f".format(item.getSubtotal())}"

        Glide.with(holder.itemView.context)
            .load(item.image)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.ivProductImage)

        holder.btnAdd.setOnClickListener {
            val newQuantity = item.quantity + 1
            onQuantityChange(item, newQuantity)
        }

        holder.btnSubtract.setOnClickListener {
            if (item.quantity > 1) {
                val newQuantity = item.quantity - 1
                onQuantityChange(item, newQuantity)
            }
        }

        holder.btnRemove.setOnClickListener {
            onRemove(item)
        }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateItems(newItems: List<CartItem>) {
        cartItems.clear()
        cartItems.addAll(newItems)
        notifyDataSetChanged()
    }
}
