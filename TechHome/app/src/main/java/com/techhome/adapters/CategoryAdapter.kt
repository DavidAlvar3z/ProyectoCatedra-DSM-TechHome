package com.techhome.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.techhome.R
import com.techhome.models.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cvCategory: CardView = view.findViewById(R.id.cvCategory)
        val vBackground: View = view.findViewById(R.id.vBackground)
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvName: TextView = view.findViewById(R.id.tvName)

        fun bind(category: Category) {
            tvName.text = category.name
            ivIcon.setImageResource(category.icon)
            vBackground.setBackgroundResource(category.gradient)

            cvCategory.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size
}