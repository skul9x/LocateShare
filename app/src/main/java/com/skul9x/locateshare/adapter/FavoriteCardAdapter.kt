package com.skul9x.locateshare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skul9x.locateshare.R
import com.skul9x.locateshare.network.FavoriteLocation

class FavoriteCardAdapter(
    private var items: MutableList<FavoriteLocation> = mutableListOf(),
    val onItemClick: (FavoriteLocation) -> Unit = {},
    val onOpenMapClick: (FavoriteLocation) -> Unit = {}
) : RecyclerView.Adapter<FavoriteCardAdapter.ViewHolder>() {

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        open var tvCardName: TextView? = view.findViewById(R.id.tvCardName)
        open var tvCardAddress: TextView? = view.findViewById(R.id.tvCardAddress)
        open var btnCardOpenMap: Button? = view.findViewById(R.id.btnCardOpenMap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        bindViewHolder(holder, item)
    }

    fun bindViewHolder(holder: ViewHolder, item: FavoriteLocation) {
        val displayName = formatDisplayName(item)

        holder.tvCardName?.text = displayName
        holder.tvCardAddress?.text = item.url

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.btnCardOpenMap?.setOnClickListener { onOpenMapClick(item) }
    }

    fun formatDisplayName(item: FavoriteLocation): String {
        return if (item.isStarred) {
            if (item.name.startsWith("⭐")) item.name else "⭐ ${item.name}"
        } else {
            item.name
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<FavoriteLocation>) {
        items.clear()
        items.addAll(newItems)
        try {
            notifyDataSetChanged()
        } catch (_: Exception) {
            // Ignore for isolated JVM unit test environments without initialized adapter observer
        }
    }

    fun getItems(): List<FavoriteLocation> = items.toList()

    fun getItem(position: Int): FavoriteLocation? = items.getOrNull(position)
}
