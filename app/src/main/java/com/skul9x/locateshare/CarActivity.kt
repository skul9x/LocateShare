package com.skul9x.locateshare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.skul9x.locateshare.network.FavoriteLocation
import com.skul9x.locateshare.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CarActivity : AppCompatActivity() {

    private lateinit var tvLocation: TextView
    private lateinit var tvLocationName: TextView
    private lateinit var btnOpenMap: Button

    private var currentUrl: String = ""

    private val api by lazy { RetrofitClient.getApiService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car)

        val rootLayout = findViewById<ConstraintLayout>(R.id.rootCarLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                statusBarInsets.left,
                statusBarInsets.top,
                statusBarInsets.right,
                statusBarInsets.bottom
            )
            insets
        }

        tvLocation = findViewById(R.id.tvLocation)
        tvLocationName = findViewById(R.id.tvLocationName)
        btnOpenMap = findViewById(R.id.btnOpenMap)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnReload = findViewById<Button>(R.id.btnReload)
        val btnFavorites = findViewById<Button>(R.id.btnFavorites)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val btnWifiSettings = findViewById<ImageButton>(R.id.btnWifiSettings)

        // Back
        btnBack.setOnClickListener { finish() }

        // Wi-Fi Settings
        btnWifiSettings.setOnClickListener { openWifiSettings() }

        // Settings
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Reload: Fetch current location from Supabase
        btnReload.setOnClickListener {
            fetchCurrentLocation()
        }

        // Open Map: Mở URL hiện tại trên bản đồ
        btnOpenMap.setOnClickListener {
            if (currentUrl.isNotEmpty()) {
                openMap(currentUrl)
            } else {
                Toast.makeText(this, "Chưa có địa điểm nào!", Toast.LENGTH_SHORT).show()
            }
        }

        // Favorites - Click ngắn: Mở địa điểm đã đánh sao ⭐
        btnFavorites.setOnClickListener {
            openStarredFavorite()
        }

        // Favorites - Long press: Hiện popup danh sách tất cả
        btnFavorites.setOnLongClickListener {
            showFavoritesPopup()
            true
        }

        // Auto-load current location khi vào
        fetchCurrentLocation()
    }

    override fun onResume() {
        super.onResume()
        // Refresh khi quay lại từ Settings
        fetchCurrentLocation()
    }

    private fun fetchCurrentLocation() {
        lifecycleScope.launch {
            try {
                val locations = withContext(Dispatchers.IO) {
                    api.getCurrentLocation()
                }
                if (locations.isNotEmpty()) {
                    val loc = locations[0]
                    if (loc.url.isNotEmpty()) {
                        currentUrl = loc.url
                        updateUI(loc.url, loc.name)
                    } else {
                        tvLocation.text = "Chưa có địa điểm nào"
                        tvLocationName.text = ""
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CarActivity, "Lỗi tải: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(url: String, name: String) {
        tvLocation.text = url
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        tvLocationName.text = if (name.isNotEmpty()) {
            "📍 $name  •  🕒 $currentTime"
        } else {
            "🕒 Cập nhật: $currentTime"
        }
    }

    private fun openStarredFavorite() {
        lifecycleScope.launch {
            try {
                val starred = withContext(Dispatchers.IO) {
                    api.getStarredFavorite()
                }
                if (starred.isNotEmpty()) {
                    openMap(starred[0].url)
                } else {
                    Toast.makeText(this@CarActivity, "Chưa có địa điểm mặc định ⭐\nẤn giữ để chọn từ danh sách", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CarActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showFavoritesPopup() {
        lifecycleScope.launch {
            try {
                val favorites = withContext(Dispatchers.IO) {
                    api.getFavorites()
                }

                if (favorites.isEmpty()) {
                    Toast.makeText(this@CarActivity, "Chưa có địa điểm ưa thích!\nVào ⚙️ Settings để thêm", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Build display names with star indicator
                val displayNames = favorites.map { fav ->
                    val star = if (fav.isStarred) "⭐ " else "    "
                    "$star${fav.name}"
                }.toTypedArray()

                AlertDialog.Builder(this@CarActivity, android.R.style.Theme_DeviceDefault_Dialog)
                    .setTitle("📍 Chọn địa điểm ưa thích")
                    .setItems(displayNames) { _, which ->
                        val selected = favorites[which]
                        openMap(selected.url)
                    }
                    .setNegativeButton("Đóng", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(this@CarActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openMap(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage("com.google.android.apps.maps")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback to browser if Maps app not installed
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể mở bản đồ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWifiSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể mở cài đặt Wi-Fi", Toast.LENGTH_SHORT).show()
        }
    }
}
