package com.skul9x.locateshare.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skul9x.locateshare.R
import com.skul9x.locateshare.network.FavoriteLocation

class FavoriteAdapter(
    private var items: MutableList<FavoriteLocation> = mutableListOf(),
    private val onStarClick: (FavoriteLocation) -> Unit,
    private val onEditClick: (FavoriteLocation) -> Unit,
    private val onDeleteClick: (FavoriteLocation) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvFavName)
        val tvUrl: TextView = view.findViewById(R.id.tvFavUrl)
        val btnStar: ImageButton = view.findViewById(R.id.btnStar)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvName.text = item.name
        holder.tvUrl.text = item.url

        // Star icon: filled yellow if starred, outline gray if not
        if (item.isStarred) {
            holder.btnStar.setImageResource(android.R.drawable.btn_star_big_on)
            holder.tvName.setTextColor(Color.parseColor("#FFD700"))
        } else {
            holder.btnStar.setImageResource(android.R.drawable.btn_star_big_off)
            holder.tvName.setTextColor(Color.WHITE)
        }

        holder.btnStar.setOnClickListener { onStarClick(item) }
        holder.btnEdit.setOnClickListener { onEditClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<FavoriteLocation>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
