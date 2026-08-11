package com.skul9x.locateshare.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.skul9x.locateshare.R
import com.skul9x.locateshare.network.FavoriteLocation

class QuickFavoriteAdapter(
    private var items: MutableList<FavoriteLocation> = mutableListOf(),
    val onItemClick: (FavoriteLocation) -> Unit = {}
) : RecyclerView.Adapter<QuickFavoriteAdapter.ViewHolder>() {

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        open var tvQuickFavName: TextView? = view.findViewById(R.id.tvQuickFavName)
        open var tvQuickFavAddress: TextView? = view.findViewById(R.id.tvQuickFavAddress)
        open var ivStarBadge: ImageView? = view.findViewById(R.id.ivStarBadge)
        open var ivQuickFavNav: ImageView? = view.findViewById(R.id.ivQuickFavNav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quick_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        bindViewHolder(holder, item)
    }

    fun bindViewHolder(holder: ViewHolder, item: FavoriteLocation) {
        val displayName = formatDisplayName(item)

        holder.tvQuickFavName?.text = displayName
        holder.tvQuickFavAddress?.text = item.url

        val starBadge = holder.ivStarBadge
        if (starBadge != null) {
            starBadge.visibility = View.VISIBLE
            if (item.isStarred) {
                starBadge.setImageResource(R.drawable.ic_star)
                try {
                    val context = starBadge.context
                    if (context != null) {
                        val goldColor = ContextCompat.getColor(context, R.color.car_starred_gold)
                        ImageViewCompat.setImageTintList(starBadge, ColorStateList.valueOf(goldColor))
                    }
                } catch (_: Exception) {
                    // Safe fallback for isolated JVM test environments without Android theme context
                }
                starBadge.contentDescription = "Starred Favorite"
            } else {
                starBadge.setImageResource(R.drawable.ic_location_pin)
                try {
                    val context = starBadge.context
                    if (context != null) {
                        val pinColor = ContextCompat.getColor(context, R.color.car_accent_pin)
                        ImageViewCompat.setImageTintList(starBadge, ColorStateList.valueOf(pinColor))
                    }
                } catch (_: Exception) {
                    // Safe fallback for isolated JVM test environments without Android theme context
                }
                starBadge.contentDescription = "Location Pin"
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    fun formatDisplayName(item: FavoriteLocation): String {
        return item.name
    }

    fun getItem(position: Int): FavoriteLocation? {
        return if (position in 0 until items.size) items[position] else null
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<FavoriteLocation>) {
        items.clear()
        items.addAll(newItems)
        try {
            notifyDataSetChanged()
        } catch (_: Exception) {
            // Safe fallback for isolated JVM unit test environments without initialized adapter observer
        }
    }

    fun getItems(): List<FavoriteLocation> = items.toList()
}
