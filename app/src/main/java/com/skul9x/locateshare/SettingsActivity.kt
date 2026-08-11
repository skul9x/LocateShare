package com.skul9x.locateshare

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skul9x.locateshare.adapter.FavoriteAdapter
import com.skul9x.locateshare.network.ApiService
import com.skul9x.locateshare.network.FavoriteLocation
import com.skul9x.locateshare.network.InsertFavorite
import com.skul9x.locateshare.network.RetrofitClient
import com.skul9x.locateshare.network.UpdateFavorite
import com.skul9x.locateshare.network.UpdateStarred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private var etFavName: EditText? = null
    private var etFavUrl: EditText? = null
    private lateinit var btnAddFavorite: Button
    private lateinit var rvFavorites: RecyclerView
    private lateinit var adapter: FavoriteAdapter

    // Left master pane status & summary views (landscape / split-screen)
    private var tvStatus: TextView? = null
    private var ivStatusDot: ImageView? = null
    private var tvFavoritesCount: TextView? = null
    private var tvStarredName: TextView? = null
    private var tvStarredUrl: TextView? = null
    private var tvVersionInfo: TextView? = null

    var apiService: ApiService? = null
    val api: ApiService get() = apiService ?: RetrofitClient.getApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_settings)

        val rootLayout = findViewById<View>(android.R.id.content)
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
                val systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                )
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        etFavName = findViewById(R.id.etFavName)
        etFavUrl = findViewById(R.id.etFavUrl)
        btnAddFavorite = findViewById(R.id.btnAddFavorite)
        rvFavorites = findViewById(R.id.rvFavorites)

        tvStatus = findViewById(R.id.tvStatus)
        ivStatusDot = findViewById(R.id.ivStatusDot)
        tvFavoritesCount = findViewById(R.id.tvFavoritesCount)
        tvStarredName = findViewById(R.id.tvStarredName)
        tvStarredUrl = findViewById(R.id.tvStarredUrl)
        tvVersionInfo = findViewById(R.id.tvVersionInfo)

        val btnBack = findViewById<View?>(R.id.btnBack) ?: findViewById<View?>(R.id.btnBackSettings)
        btnBack?.setOnClickListener { finish() }

        // Setup RecyclerView
        adapter = FavoriteAdapter(
            onStarClick = { item -> toggleStar(item) },
            onEditClick = { item -> showEditDialog(item) },
            onDeleteClick = { item -> confirmDelete(item) }
        )
        rvFavorites.layoutManager = LinearLayoutManager(this)
        rvFavorites.isNestedScrollingEnabled = false
        rvFavorites.adapter = adapter

        // Add button
        btnAddFavorite.setOnClickListener { onAddFavoriteClicked() }

        // Load favorites
        loadFavorites()
    }

    private fun onAddFavoriteClicked() {
        val inlineName = etFavName?.text?.toString()?.trim() ?: ""
        val inlineUrl = etFavUrl?.text?.toString()?.trim() ?: ""

        if (inlineName.isNotEmpty() && inlineUrl.isNotEmpty()) {
            addFavorite(inlineName, inlineUrl)
        } else {
            showAddFavoriteDialog()
        }
    }

    fun showAddFavoriteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_favorite, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val etName = dialogView.findViewById<EditText>(R.id.etEditName)
        val etUrl = dialogView.findViewById<EditText>(R.id.etEditUrl)

        tvTitle?.text = "➕ Thêm địa điểm mới"
        etName?.hint = "Tên địa điểm (VD: Nhà, Công ty...)"
        etUrl?.hint = "Link Google Maps hoặc tọa độ"

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Thêm") { _, _ ->
                val name = etName.text.toString().trim()
                val url = etUrl.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập tên địa điểm", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (url.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập link Google Maps", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                addFavorite(name, url)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    fun loadFavorites() {
        lifecycleScope.launch {
            try {
                val favorites = withContext(Dispatchers.IO) {
                    api.getFavorites()
                }
                adapter.updateList(favorites)
                updateStarredSummary(favorites)
                updateFavoritesCount(favorites.size)
                updateConnectionStatus(isOnline = true, message = "Đã kết nối Supabase")
            } catch (e: Exception) {
                updateConnectionStatus(isOnline = false, message = "Lỗi kết nối: ${e.message}")
                Toast.makeText(this@SettingsActivity, "Lỗi tải danh sách: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addFavorite(name: String = etFavName?.text?.toString()?.trim() ?: "", url: String = etFavUrl?.text?.toString()?.trim() ?: "") {
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
                etFavName?.text?.clear()
                etFavUrl?.text?.clear()
                Toast.makeText(this@SettingsActivity, "Đã thêm: $name", Toast.LENGTH_SHORT).show()
                loadFavorites()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Lỗi thêm: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toggleStar(item: FavoriteLocation) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (!item.isStarred) {
                        // Unstar all others first (single-starred invariant)
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

    fun updateStarredSummary(items: List<FavoriteLocation>) {
        val starred = items.find { it.isStarred }
        if (starred != null) {
            tvStarredName?.text = starred.name
            tvStarredName?.setTextColor(Color.parseColor("#FFD700"))
            tvStarredUrl?.text = starred.url
        } else {
            tvStarredName?.text = "Chưa có địa điểm mặc định ⭐"
            tvStarredName?.setTextColor(Color.WHITE)
            tvStarredUrl?.text = "Bấm ⭐ trong danh sách để chọn nhanh"
        }
    }

    fun updateFavoritesCount(count: Int) {
        tvFavoritesCount?.text = "$count địa điểm"
    }

    fun updateConnectionStatus(isOnline: Boolean, message: String) {
        tvStatus?.text = message
        tvStatus?.setTextColor(if (isOnline) Color.parseColor("#00E676") else Color.parseColor("#FF5252"))
        ivStatusDot?.setImageResource(if (isOnline) R.drawable.bg_status_dot_connected else R.drawable.bg_status_dot_disconnected)
    }

    fun showEditDialog(item: FavoriteLocation) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_favorite, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val etEditName = dialogView.findViewById<EditText>(R.id.etEditName)
        val etEditUrl = dialogView.findViewById<EditText>(R.id.etEditUrl)

        tvTitle?.text = "✏️ Sửa địa điểm"
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

    fun updateFavorite(id: Long, name: String, url: String) {
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

    fun confirmDelete(item: FavoriteLocation) {
        AlertDialog.Builder(this)
            .setTitle("Xóa địa điểm")
            .setMessage("Bạn có chắc muốn xóa \"${item.name}\"?")
            .setPositiveButton("Xóa") { _, _ -> deleteFavorite(item) }
            .setNegativeButton("Hủy", null)
            .show()
    }

    fun deleteFavorite(item: FavoriteLocation) {
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
