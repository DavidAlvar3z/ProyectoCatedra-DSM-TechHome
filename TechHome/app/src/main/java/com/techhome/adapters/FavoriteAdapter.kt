package com.techhome.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.techhome.R
import com.techhome.models.Favorite
import java.text.NumberFormat
import java.util.*

class FavoriteAdapter(
    private var favorites: List<Favorite>,
    private val onItemClick: (Favorite) -> Unit,
    private val onRemoveClick: (Favorite) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    companion object {
        private const val TAG = "FavoriteAdapter"
    }

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView as CardView
        val ivProduct: ImageView = itemView.findViewById(R.id.ivProduct)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val tvStock: TextView = itemView.findViewById(R.id.tvStock)
        val cvStock: CardView = itemView.findViewById(R.id.cvStock)
        val btnRemoveFavorite: ImageButton = itemView.findViewById(R.id.btnRemoveFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favorite = favorites[position]

        Log.d(TAG, "🎨 Binding favorito en posición $position: ${favorite.productName}")

        // Nombre del producto
        holder.tvProductName.text = favorite.productName

        // Precio
        holder.tvPrice.text = formatter.format(favorite.price)

        // Imagen
        Glide.with(holder.itemView.context)
            .load(favorite.productImage)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(holder.ivProduct)

        // Rating (oculto por defecto, se puede actualizar después)
        holder.tvRating.visibility = View.GONE

        // Stock (oculto por defecto)
        holder.cvStock.visibility = View.GONE

        // Click en el card
        holder.cardView.setOnClickListener {
            Log.d(TAG, "👆 Click en favorito: ${favorite.productName}")
            onItemClick(favorite)
        }

        // Click en botón de eliminar
        holder.btnRemoveFavorite.setOnClickListener {
            Log.d(TAG, "🗑️ Click en eliminar favorito: ${favorite.productName}")
            onRemoveClick(favorite)
        }
    }

    override fun getItemCount(): Int = favorites.size

    /**
     * ✅ Actualizar lista completa
     */
    fun updateFavorites(newFavorites: List<Favorite>) {
        Log.d(TAG, "🔄 Actualizando favoritos: ${newFavorites.size} items")

        // ✅ Filtrar duplicados por SKU antes de actualizar
        val uniqueFavorites = newFavorites.distinctBy { it.productSku }

        if (uniqueFavorites.size != newFavorites.size) {
            Log.w(TAG, "⚠️ Se detectaron y eliminaron duplicados: ${newFavorites.size - uniqueFavorites.size}")
        }

        favorites = uniqueFavorites
        notifyDataSetChanged()
    }

    /**
     * Eliminar un favorito específico
     */
    fun removeFavorite(favorite: Favorite) {
        val position = favorites.indexOf(favorite)
        if (position != -1) {
            Log.d(TAG, "🗑️ Eliminando favorito en posición $position")
            favorites = favorites.toMutableList().apply { removeAt(position) }
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, favorites.size)
        }
    }
}