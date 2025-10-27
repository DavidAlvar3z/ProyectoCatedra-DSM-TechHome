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
import java.text.NumberFormat
import java.util.*

class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    private val onQuantityChange: (CartItem, Int) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)
        val btnAdd: ImageButton = itemView.findViewById(R.id.btnAdd)
        val btnSubtract: ImageButton = itemView.findViewById(R.id.btnSubtract)
        val btnRemove: View = itemView.findViewById(R.id.btnRemove) // CardView que contiene el botón
        val btnRemoveInner: ImageButton? = try {
            // Intentar obtener el ImageButton dentro del CardView
            (btnRemove as? ViewGroup)?.getChildAt(0) as? ImageButton
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]

        // Mostrar información del producto
        holder.tvProductName.text = item.productName
        holder.tvPrice.text = formatter.format(item.price)
        holder.tvQuantity.text = item.quantity.toString()
        holder.tvSubtotal.text = formatter.format(item.getSubtotal())

        // Cargar imagen
        Glide.with(holder.itemView.context)
            .load(item.productImage)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(holder.ivProductImage)

        // Botón aumentar cantidad
        holder.btnAdd.setOnClickListener {
            val newQuantity = item.quantity + 1
            onQuantityChange(item, newQuantity)
        }

        // Botón disminuir cantidad
        holder.btnSubtract.setOnClickListener {
            if (item.quantity > 1) {
                val newQuantity = item.quantity - 1
                onQuantityChange(item, newQuantity)
            }
        }

        // ✅ SOLUCIÓN: Asignar listener tanto al CardView como al ImageButton
        val removeClickListener = View.OnClickListener {
            onRemove(item)
        }

        // Asignar al CardView
        holder.btnRemove.setOnClickListener(removeClickListener)

        // Y también al ImageButton interno si existe
        holder.btnRemoveInner?.setOnClickListener(removeClickListener)
    }

    override fun getItemCount(): Int = cartItems.size

    /**
     * Actualizar la lista de items del carrito
     */
    fun updateItems(newItems: List<CartItem>) {
        cartItems.clear()
        cartItems.addAll(newItems)
        notifyDataSetChanged()
    }

    /**
     * Eliminar un item específico
     */
    fun removeItem(item: CartItem) {
        val position = cartItems.indexOf(item)
        if (position != -1) {
            cartItems.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, cartItems.size)
        }
    }

    /**
     * Actualizar cantidad de un item específico
     */
    fun updateItemQuantity(item: CartItem, newQuantity: Int) {
        val position = cartItems.indexOfFirst { it.cartItemId == item.cartItemId }
        if (position != -1) {
            cartItems[position] = item.copy(quantity = newQuantity)
            notifyItemChanged(position)
        }
    }
}