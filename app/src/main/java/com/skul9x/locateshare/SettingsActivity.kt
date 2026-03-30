package com.skul9x.locateshare

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skul9x.locateshare.adapter.FavoriteAdapter
import com.skul9x.locateshare.network.FavoriteLocation
import com.skul9x.locateshare.network.InsertFavorite
import com.skul9x.locateshare.network.RetrofitClient
import com.skul9x.locateshare.network.UpdateFavorite
import com.skul9x.locateshare.network.UpdateStarred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var etFavName: EditText
    private lateinit var etFavUrl: EditText
    private lateinit var btnAddFavorite: Button
    private lateinit var rvFavorites: RecyclerView
    private lateinit var adapter: FavoriteAdapter

    private val api by lazy { RetrofitClient.getApiService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        etFavName = findViewById(R.id.etFavName)
        etFavUrl = findViewById(R.id.etFavUrl)
        btnAddFavorite = findViewById(R.id.btnAddFavorite)
        rvFavorites = findViewById(R.id.rvFavorites)

        val btnBack = findViewById<Button>(R.id.btnBackSettings)
        btnBack.setOnClickListener { finish() }

        // Setup RecyclerView
        adapter = FavoriteAdapter(
            onStarClick = { item -> toggleStar(item) },
            onEditClick = { item -> showEditDialog(item) },
            onDeleteClick = { item -> confirmDelete(item) }
        )
        rvFavorites.layoutManager = LinearLayoutManager(this)
        rvFavorites.adapter = adapter

        // Add button
        btnAddFavorite.setOnClickListener { addFavorite() }

        // Load favorites
        loadFavorites()
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            try {
                val favorites = withContext(Dispatchers.IO) {
                    api.getFavorites()
                }
                adapter.updateList(favorites)
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Lỗi tải danh sách: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addFavorite() {
        val name = etFavName.text.toString().trim()
        val url = etFavUrl.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên địa điểm", Toast.LENGTH_SHORT).show()
            return
        }
        if (url.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập link Google Maps", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    api.addFavorite(InsertFavorite(name = name, url = url))
                }
                etFavName.text.clear()
                etFavUrl.text.clear()
                Toast.makeText(this@SettingsActivity, "Đã thêm: $name", Toast.LENGTH_SHORT).show()
                loadFavorites()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Lỗi thêm: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleStar(item: FavoriteLocation) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (!item.isStarred) {
                        // Unstar all others first
                        api.unstarAll(body = UpdateStarred(isStarred = false))
                    }
                    // Toggle this item's star
                    api.updateFavorite(
                        id = "eq.${item.id}",
                        body = UpdateFavorite(isStarred = !item.isStarred)
                    )
                }
                loadFavorites()
                val msg = if (!item.isStarred) "⭐ ${item.name} là mặc định!" else "Đã bỏ sao"
                Toast.makeText(this@SettingsActivity, msg, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditDialog(item: FavoriteLocation) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_favorite, null)
        val etEditName = dialogView.findViewById<EditText>(R.id.etEditName)
        val etEditUrl = dialogView.findViewById<EditText>(R.id.etEditUrl)

        etEditName.setText(item.name)
        etEditUrl.setText(item.url)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val newName = etEditName.text.toString().trim()
                val newUrl = etEditUrl.text.toString().trim()
                if (newName.isNotEmpty() && newUrl.isNotEmpty()) {
                    updateFavorite(item.id, newName, newUrl)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateFavorite(id: Long, name: String, url: String) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    api.updateFavorite(
                        id = "eq.$id",
                        body = UpdateFavorite(name = name, url = url)
                    )
                }
                Toast.makeText(this@SettingsActivity, "Đã cập nhật!", Toast.LENGTH_SHORT).show()
                loadFavorites()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete(item: FavoriteLocation) {
        AlertDialog.Builder(this)
            .setTitle("Xóa địa điểm")
            .setMessage("Bạn có chắc muốn xóa \"${item.name}\"?")
            .setPositiveButton("Xóa") { _, _ -> deleteFavorite(item) }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteFavorite(item: FavoriteLocation) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    api.deleteFavorite(id = "eq.${item.id}")
                }
                Toast.makeText(this@SettingsActivity, "Đã xóa: ${item.name}", Toast.LENGTH_SHORT).show()
                loadFavorites()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
